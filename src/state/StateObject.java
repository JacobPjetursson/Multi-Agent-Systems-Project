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


	public boolean isNeighbor(Location loc) {
        if (location.getRow() == loc.getRow() && location.getCol() == loc.getCol() + 1)
            return true;
        else if (location.getRow() == loc.getRow() && location.getCol() == loc.getCol() - 1)
            return true;
        else if (location.getRow() == loc.getRow() + 1 && location.getCol() == loc.getCol())
            return true;
        else if (location.getRow() == loc.getRow() - 1 && location.getCol() == loc.getCol())
            return true;
        else if (location.getRow() == loc.getRow() + 1 && location.getCol() == loc.getCol() + 1)
            return true;
        else if (location.getRow() == loc.getRow() + 1 && location.getCol() == loc.getCol() - 1)
            return true;
        else if (location.getRow() == loc.getRow() - 1 && location.getCol() == loc.getCol() - 1)
			return true;
		else return location.getRow() == loc.getRow() - 1 && location.getCol() == loc.getCol() + 1;

	}
}
