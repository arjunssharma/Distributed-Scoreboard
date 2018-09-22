import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZookeeperWatcher implements Watcher { //can use CuratorWatcher as well

	private ZooKeeper zookeeper;
	private final CountDownLatch connectedSignal = new CountDownLatch(1);
	private final int sessionTimeout = 3000;
	private String host;
	private final static Logger LOGGER = Logger.getLogger(ZookeeperWatcher.class.getName());
	
	int list_size;
	
	public ZookeeperWatcher(String connectString, int list_size) {
		this.list_size = list_size;
		this.host = connectString;
	}

	public ZooKeeper start() {
		
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
			zookeeper = new ZooKeeper(host, sessionTimeout, new Watcher() {
				
				@Override
				public void process(WatchedEvent arg0) {
					// TODO Auto-generated method stub
					connectedSignal.countDown();
				}
			});
			
			connectedSignal.await();
						
			Stat stat = zookeeper.exists("/data/all", this);
			if (stat != null) {
				zookeeper.getChildren("/data/all", this);
			}

		}
		catch(Exception e) {
			LOGGER.warning("Something is wrong while instantiating watcher");
		}
		
		return zookeeper;
	}
	
	//path = "/data/all"
	public synchronized void display_sorted_list(ZooKeeper zoo, String path) throws Exception {
		List<String> list = zoo.getChildren(path, false);
		List<Player> players = new ArrayList<>();
		for(String node_name : list) {
			byte [] b = zoo.getData(path + "/" + node_name, false, null);
			String str = new String(b, "UTF-8");
			String name = str.split(" ")[0];
			if(name.contains("_"))
				name = name.replace("_", " ");
			int score = Integer.valueOf(str.split(" ")[1]);
			Player p = new Player(name, score);
			players.add(p);
		}
		
		Collections.sort(players);
		
		Set<String> active_players = get_active_players(zoo, "/data/eph");
		
		System.out.println("Highest scores");
		System.out.println("---------------");
		for(int i = 0; i < list_size && i < players.size(); i++) {
			Player p = players.get(i);
			System.out.print(p.getName() + "\t" + p.getScore());
			if(active_players.contains(p.getName()))
				System.out.print(" **");
			System.out.println("\n");
		}
	}
	
	//path = "/data/eph"
	public synchronized Set<String> get_active_players(ZooKeeper zoo, String path) throws Exception {
		Set<String> result = new HashSet<>();
		List<String> list = zoo.getChildren(path, false);
		for(String node_name : list) {
			byte [] b = zoo.getData(path + "/" + node_name, false, null);
			String str = new String(b, "UTF-8");
			String name = str.split(" ")[0];
			if(name.contains("_"))
				name = name.replace("_", " ");
			result.add(name);
		}
		return result;
	}
	
	//path = "/data/all
	public synchronized void display_most_recent_list(ZooKeeper zoo, String path) throws Exception {
		List<String> list = zoo.getChildren(path, false);
		TreeMap<String, String> map = new TreeMap<>(Collections.reverseOrder());
		//Stat stat = new Stat();
		for(String node_name : list) {
			byte [] b = zoo.getData(path + "/" + node_name, false, null);
			String str = new String(b, "UTF-8");
			map.put(node_name, str);
		}
		
		Set<String> active_players = get_active_players(zoo, "/data/eph");
		System.out.println("Most recent scores");
		System.out.println("------------------");
		int i = 0;
		for(String key : map.keySet()) {
			if(i >= list_size)
				break;
			
			String value = map.get(key);
			String player_name = value.split(" ")[0];
			
			if(player_name.contains("_"))
				player_name = player_name.replace("_", " ");
			
			String score = value.split(" ")[1];
			System.out.print(player_name + "\t" + score);
			if(active_players.contains(player_name))
				System.out.print(" **");
			System.out.println("\n");
			i++;
		}
	}

	@Override
	public void process(WatchedEvent event) {
		EventType eventType = event.getType();
		if (eventType == EventType.NodeChildrenChanged || eventType == EventType.NodeCreated) {
			try {
				display_most_recent_list(zookeeper, "/data/all");
				display_sorted_list(zookeeper, "/data/all");
			} catch (Exception e) {
				LOGGER.warning("Error Displaying Node Data");
			}
		}
		try {
			zookeeper.getChildren("/data/all", this);
		} 
		catch(Exception e) {
			LOGGER.warning("Error Attaching Watcher to Child Nodes ");
		}	
	}
}
