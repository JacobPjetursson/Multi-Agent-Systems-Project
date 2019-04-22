package state;

import java.util.Objects;

import action.Action;

public class Location {
	
	private int row;
	private int col;
	
	public Location(int row, int col) {
		this.row = row;
		this.col = col;
	}

	public Location(Location duplicate) {
	    this.row = duplicate.getRow();
	    this.col = duplicate.getCol();
    }
	
	public Location move(Action.Dir direction) {
		Location location = new Location(this);
		location.row += Action.dirToRowChange(direction);
		location.col += Action.dirToColChange(direction);
		return location;
	}

    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof Location) {
			Location location = (Location) obj;
			return this.row == location.row && this.col == location.col;
		}
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRow(), getCol());
    }

    int getRow() {
		return row;
	}

	int getCol() {
		return col;
	}

	/* Do not uncomment. Location is an immutable object
	public void setRow(int row) {
	    this.row = row;
    }

    public void setCol(int col) {
	    this.col = col;
    }
    */
	
	@Override
	public String toString() {
		return "["+row+";"+col+"]";
	}

}
