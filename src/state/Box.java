package state;

public class Box extends MovableObject {
	
	private char letter;

	public Box(Location location, int color, char letter) {
		super(location, color);
		this.letter = letter;
	}

	public Box(Box duplicate) {
	    super(new Location(duplicate.getLocation()), duplicate.getColor());
	    this.letter = duplicate.getLetter();
    }
	
	public char getLetter() {
		return letter;
	}

	@Override
	public Box move(Location position) {
		return new Box(position, color, letter);
	}
	
	@Override
	public String toString() {
		return "Box " + letter + ";" + color + " - " + position;
	}

}
