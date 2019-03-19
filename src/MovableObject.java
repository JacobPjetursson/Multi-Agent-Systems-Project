import java.awt.Color;
import java.awt.Point;

public abstract class MovableObject extends StateObject {

	public MovableObject(Point position, Color color) {
		super(position, color);
	}
	
	public abstract MovableObject move(Point position);

}
