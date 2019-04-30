package state;

public abstract class Goal extends StateObject {
	private char letter;
	private MovableObject assignedObj;

	public Goal(Location position, int color, char letter) {
		super(position, color);
		this.letter = letter;
	}

	public abstract void assignObj(MovableObject obj);

	public abstract MovableObject getAssignedObj();
	
	public char getLetter() {
		return letter;
	}
	
	@Override
	public String toString() {
		return "Goal " + letter + "<" + color + "> - " + location;
	}
	
}
