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
    
    int getId() {
    	return id;
    }
	
	@Override
	public String toString() {
		return "Box " + letter + "<" + color + "> - " + location;
	}

}
