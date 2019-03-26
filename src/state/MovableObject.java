package state;

import action.Action;

public abstract class MovableObject extends StateObject {

	public MovableObject(Location location, int color) {
		super(location, color);
	}

	public void move(Action.Dir dir) {
	    switch(dir) {
            case N:
                location.setRow(location.getRow() - 1);
                break;
            case S:
                location.setRow(location.getRow() + 1);
                break;
            case E:
                location.setRow(location.getCol() + 1);
                break;
            case W:
                location.setCol(location.getCol() - 1);
                break;
        }
    }

}
