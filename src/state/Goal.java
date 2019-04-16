package state;

public class Goal extends StateObject {
	
	private char letter;

	public Goal(Location position, int color, char letter) {
		super(position, color);
		this.letter = letter;
	}
	
	public char getLetter() {
		return letter;
	}
	
	@Override
	public String toString() {
		return "Goal " + letter + "<" + color + "> - " + location;
	}
	
}
