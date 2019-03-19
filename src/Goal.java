import java.awt.Color;
import java.awt.Point;

public class Goal extends StateObject {
	
	private char letter;

	public Goal(Point position, Color color, char letter) {
		super(position, color);
		this.letter = letter;
	}
	
	public char getLetter() {
		return letter;
	}
	
}
