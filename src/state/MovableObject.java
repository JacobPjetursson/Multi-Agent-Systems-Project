package state;

import action.Action;

public abstract class MovableObject extends StateObject {

	public MovableObject(Location location, int color) {
		super(location, color);
	}
	
	public void setLocation(Location location) {
		this.location = location;
	}

	public void move(Action.Dir dir) {
		location = location.move(dir);
    }

}
