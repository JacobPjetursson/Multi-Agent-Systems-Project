package state;

import java.util.Objects;

public class Box extends MovableObject {
	
	private char letter;
	private int id;

	public Box(int id, int color, char letter, Location location) {
		super(location, color);
		this.letter = letter;
		this.id = id;
	}

	public Box(Box duplicate) {
		this(duplicate.getId(), duplicate.getColor(), 
				duplicate.getLetter(), duplicate.getLocation());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Box)) return false;
        Box box = (Box) o;
        return getLetter() == box.getLetter() && getLocation().equals(box.getLocation());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getLetter(), getLocation());
    }

    public char getLetter() {
		return letter;
	}
    
    public int getId() {
    	return id;
    }
	
	@Override
	public String toString() {
		return "Box " + letter + "<" + color + "> - " + location;
	}

	public boolean isSafe() {
		int wallCount = 0;
		int row = location.getRow();
		int col = location.getCol();
		boolean up = State.walls[row-1][col];
		boolean down = State.walls[row+1][col];
		boolean left = State.walls[row][col-1];
		boolean right = State.walls[row][col+1];
		boolean upLeft = State.walls[row - 1][col - 1];
		boolean upRight = State.walls[row - 1][col + 1];
		boolean downLeft = State.walls[row + 1][col - 1];
		boolean downRight = State.walls[row + 1][col + 1];
		wallCount += up ? 1 : 0;
		wallCount += down ? 1 : 0;
		wallCount += left ? 1 : 0;
		wallCount += right ? 1 : 0;

		if(wallCount >= 3) {
			return true;
		}

		else if(wallCount == 2) {
			if (up && right) {
				return !downLeft;
			} else if (right && down) {
				return !upLeft;
			} else if (down && left) {
				return !upRight;
			} else if (left && up) {
				return !downRight;
			}
		}

		else if (wallCount == 1) {
			if (up)
				return !downRight && !downLeft;
			if (down)
				return !upRight && !upLeft;
			if (left)
				return !upRight && !downRight;
			if (right)
				return !upLeft && !downLeft;

		}
		else if(wallCount == 0) {
			int corners = 0;
			corners += upLeft ? 1 : 0;
			corners += upRight ? 1 : 0;
			corners += downLeft ? 1 : 0;
			corners += downRight ? 1 : 0;
			return corners < 1;
		}

		return false;
	}

}
