package state;

public class Box extends MovableObject {
	
	private char letter;

	public Box(Location position, int color, char letter) {
		super(position, color);
		this.letter = letter;
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
