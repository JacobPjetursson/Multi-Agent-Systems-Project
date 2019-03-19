import java.awt.Color;
import java.awt.Point;

public class MovableObject extends StateObject {

	public MovableObject(Point position, Color color) {
		super(position, color);
	}
	
	public MovableObject move(Point position) {
		return new MovableObject(position, color);
	}

}
