package state;

import java.util.Objects;

public class Box extends MovableObject {
	
	private char letter;

	public Box(Location location, int color, char letter) {
		super(location, color);
		this.letter = letter;
	}

	Box(Box duplicate) {
	    super(new Location(duplicate.getLocation()), duplicate.getColor());
	    this.letter = duplicate.getLetter();
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
	
	@Override
	public String toString() {
		return "Box " + letter + ";" + color + " - " + location;
	}

}
