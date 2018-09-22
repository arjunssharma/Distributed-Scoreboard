import java.io.Serializable;

//java Pojo for player name and score testing
public class Player implements Serializable, Comparable<Player> {
	
	private static final long serialVersionUID = 1L;
	private String name;
	private int score;
	
	public Player(String name, int score) {
		super();
		this.name = name;
		this.score = score;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	//sort in descending order
	@Override
	public int compareTo(Player player) {
		return player.getScore() - this.getScore();
	}
	
	//Main Module for testing
	/*
	public static void main(String args[]) {
		Player p1 = new Player("Arjun", 100);
		Player p2 = new Player("Arjun", 300);
		Player p3 = new Player("Arjun", 200);
		Player p4 = new Player("Arjun", 500);
		Player p5 = new Player("Arjun", 900);
		List<Player> list = new ArrayList<>();
		list.add(p1); list.add(p2); list.add(p3); list.add(p4); list.add(p5);
		
		Collections.sort(list);
		
		for(Player p : list) {
			System.out.println(p.getName() + " : " + p.getScore());
		}
	}
	*/
}
