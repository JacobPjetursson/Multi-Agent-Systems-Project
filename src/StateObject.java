import java.awt.Color;
import java.awt.Point;

public abstract class StateObject {
	protected Point position;
	protected Color color;
	
	public StateObject(Point position, Color color) {
		this.position = position;
		this.color = color;
	}
	
	public Point getPosition() {
		return position;
	}
	
	public Color getColor() {
		return color;
	}
	
}
