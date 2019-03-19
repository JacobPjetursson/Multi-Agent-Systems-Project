import java.awt.Color;
import java.awt.Point;

public class Agent extends MovableObject {
	
	private int id;

	public Agent(Point position, Color color, int id) {
		super(position, color);
		this.id = id;
	}
	
	public int getId() {
		return id;
	}

}
