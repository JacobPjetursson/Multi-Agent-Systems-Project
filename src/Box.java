import java.awt.Color;
import java.awt.Point;

public class Box extends MovableObject {
	
	private char letter;

	public Box(Point position, Color color, char letter) {
		super(position, color);
		this.letter = letter;
	}
	
	public char getLetter() {
		return letter;
	}

	@Override
	public Box move(Point position) {
		return new Box(position, color, letter);
	}

}
