import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;


public class PlayerClient {

	private ZooKeeper zookeeper;
	private final CountDownLatch connectedSignal = new CountDownLatch(1);
	private final int sessionTimeOut = 3000;
	private final static Logger LOGGER = Logger.getLogger(PlayerClient.class.getName());
	private final static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
	
	public ZooKeeper connect(String host) {
		if(!host.contains(":")) {
			host += ":6000";
		}
		else if(host.contains(":")) {
			int index = host.indexOf(':');
			String temp = host.substring(index + 1);
			if(temp.length() == 0)
				host += "6000";
		}
		
		try {
			zookeeper = new ZooKeeper(host, sessionTimeOut, new Watcher() {
				public void process(WatchedEvent we) {
					if (we.getState() == KeeperState.SyncConnected) {
						connectedSignal.countDown();
					}
				}
			});
			connectedSignal.await();
		}
		catch(InterruptedException | IOException e) {
			LOGGER.warning("Error while creating zookeeper object or during connected signal await");
		}
		
		//null check in main
		return zookeeper;
	}

	
	public void close() throws InterruptedException {
		zookeeper.close();
	}
	
	public static void main(String args[]) {
		PlayerClient player_connection = null;
		ZookeeperWatcher watcher_connection = null;

		//Testing Purpose
/*    
		String line = sc.nextLine();
		Map<String, String> map = connection.readCommands(line);
		for(String key : map.keySet())
			System.out.println(key + " --> " + map.get(key));
*/		
		String base_node = ZKPaths.makePath(ZKPaths.PATH_SEPARATOR, "data");
		String all_scores = ZKPaths.makePath(base_node, "all"); // for high scores and most recent scores
		String players = ZKPaths.makePath(all_scores, "player-");
		String active = ZKPaths.makePath(base_node, "eph"); //for active users
		String ephi_active = ZKPaths.makePath(active, "player-");
		
		System.out.println("Please Enter Commands back to back");
		
		block: {
			while (true) {
				String line = null;
				try {
					line = br.readLine();
				} catch (IOException e1) {
					LOGGER.warning("Error reading command");
				}
				Map<String, String> commands = readCommands(line);
				try {
					if (commands.get("client").equalsIgnoreCase("player")) {
						String host = commands.get("host");
						if (host == null || host.equals("") || host.length() == 0)
							continue;

						Integer count = null;
						if(commands.get("count") != null) 
							count = Integer.valueOf(commands.get("count"));
						
						Integer score = null;
						if (commands.get("score") != null)
							score = Integer.valueOf(commands.get("score"));
						else {
							System.out.println("Enter Player Score");
							score = Integer.valueOf(br.readLine());
						}
						
						if (score < 0) {
							LOGGER.warning("Negative Score! Please insert positive score");
							System.out.println("Please Enter Command");
							continue;
						}
						
						String player_name = commands.get("name");
						
						
						//batch mode
						if(count != null) {
							ZooKeeper zk = null;
							Integer delay = null;
							if(commands.get("delay") != null)
								delay = Integer.valueOf(commands.get("delay"));
							else
								delay = 2; //randomly assigned assumption
								
							
							for(int i = 0; i < count; i++) {
								Random random_delay_object = new Random(delay * 1000);
								Thread.sleep(Math.round(random_delay_object.nextGaussian() + delay * 1000));
								
								Random random_score_object = new Random(score++);
								int random_score = new Double(random_score_object.nextGaussian() + score).intValue();
								
								player_connection = new PlayerClient();
								zk = player_connection.connect(host);
								
								// path = /data/all/player-
								if (zk.exists(base_node, false) == null)
									zk.create(base_node, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
								
								if(zk.exists(all_scores, false) == null)
									zk.create(all_scores, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
								
								if (player_name != null && score != null) {
									ByteArrayOutputStream output = new ByteArrayOutputStream();
									output.write((player_name + " " + random_score).getBytes());
									zk.create(players, output.toByteArray(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
								}		
								
								
								// path = /data/eph
								if(zk.exists(active, false) == null)
									zk.create(active, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
								
								if(score != null)
									zk.create(ephi_active, player_name.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
							}

							break block;
						}
								
						player_connection = new PlayerClient();
						ZooKeeper zk = player_connection.connect(host);
						
						//for highest scores and most recent scores
						if (zk.exists(base_node, false) == null)
							zk.create(base_node, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);

						if(zk.exists(all_scores, false) == null)
							zk.create(all_scores, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						
						if (player_name != null && score != null)
							zk.create(players, (player_name + " " + score).getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
						
						
						//for active users
						if(zk.exists(active, false) == null)
							zk.create(active, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
						
						if(score != null)
							zk.create(ephi_active, player_name.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
					
					}

					else if (commands.get("client").equalsIgnoreCase("watcher")) {
						String host = commands.get("host");
						if (host == null || host.equals("") || host.length() == 0)
							continue;

						Integer list_size = null;
						if (commands.get("score_list_size") != null) {
							try {
								list_size = Integer.valueOf(commands.get("score_list_size"));
							}
							catch(Exception e) {
								LOGGER.warning("Invalid List Size N, Re-enter the command");
								continue;
							}
						}
							

						watcher_connection = new ZookeeperWatcher(host, list_size);
						watcher_connection.start();
					}
				} catch (Exception e) {
					LOGGER.warning("Something is wrong " + e.getMessage());
				}
			}
		}
	}
	
	
	public static Map<String, String> readCommands(String line) {
		synchronized(Thread.class) {
			line = line.trim();
			StringBuffer sb = new StringBuffer(line);
			if (line.contains("\"")) {
				int start_index = line.indexOf("\"");
				int last_index = line.lastIndexOf("\"");
				String full_name = line.substring(start_index, last_index);
				full_name = full_name.replace("\"", "");
				line = line.replace("\"" + full_name + "\"", "");
				full_name = full_name.split(" ")[0] + "_" + full_name.split(" ")[1];
				sb = new StringBuffer(line);
				sb.insert(start_index, full_name);
			}
			
			line = sb.toString();

			String split[] = line.split(" ");
			Map<String, String> commands = new LinkedHashMap<>();

			if (split[0].equalsIgnoreCase("player")) {
				for (int i = 0; i < split.length; i++) {
					String s = split[i];
					s = s.trim();
					if (i == 0)
						commands.put("client", s);
					else if (i == 1 && s.contains("."))
						commands.put("host", s);
					else if (i == 2)
						commands.put("name", s);
					else if (i == 3)
						commands.put("count", s);
					else if (i == 4)
						commands.put("delay", s);
					else if (i == 5)
						commands.put("score", s);
				}
			}

			else if (split[0].equalsIgnoreCase("watcher")) {
				for (int i = 0; i < split.length; i++) {
					String s = split[i];
					s = s.trim();
					if (i == 0)
						commands.put("client", s);
					else if (i == 1 && s.contains("."))
						commands.put("host", s);
					else if (i == 2)
						commands.put("score_list_size", s);
				}
			}

			return commands;
		}
	}
}
