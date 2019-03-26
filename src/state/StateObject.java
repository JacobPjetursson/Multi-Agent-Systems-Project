package state;

public abstract class StateObject {
	protected Location location;
	protected int color;
	
	public StateObject(Location location, int color) {
		this.location = location;
		this.color = color;
	}
	
	public Location getLocation() {
		return location;
	}
	
	public int getColor() {
		return color;
	}
	
}
