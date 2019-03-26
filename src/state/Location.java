package state;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;
        Location location = (Location) o;
        return getRow() == location.getRow() &&
                getCol() == location.getCol();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRow(), getCol());
    }

    public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public void setRow(int row) {
	    this.row = row;
    }

    public void setCol(int col) {
	    this.col = col;
    }
	
	@Override
	public String toString() {
		return "["+row+";"+col+"]";
	}

}
