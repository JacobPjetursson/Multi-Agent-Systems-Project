package state;

public abstract class MovableObject extends StateObject {

	public MovableObject(Location position, int color) {
		super(position, color);
	}
	
	public abstract MovableObject move(Location position);

}
