package state;

public abstract class StateObject {
	protected Location position;
	protected int color;
	
	public StateObject(Location position, int color) {
		this.position = position;
		this.color = color;
	}
	
	public Location getPosition() {
		return position;
	}
	
	public int getColor() {
		return color;
	}
	
}
