package state;

public class Goal extends StateObject {
	
	private char letter;
	private Box assignedBox;

	public Goal(Location position, int color, char letter) {
		super(position, color);
		this.letter = letter;
	}
	
	public char getLetter() {
		return letter;
	}

	public void assignBox(Box box) {
	    this.assignedBox = box;
    }

    public Box getAssignedBox() {
	    return assignedBox;
    }
	
	@Override
	public String toString() {
		return "Goal " + letter + "<" + color + "> - " + location;
	}
	
}
