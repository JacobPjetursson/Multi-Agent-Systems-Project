package state;

import java.util.Objects;

public class Agent extends MovableObject {
	
	private int id;

	public Agent(Location position, int color, int id) {
		super(position, color);
		this.id = id;
	}

	public Agent(Agent duplicate) {
	    super(new Location(duplicate.getLocation()), duplicate.getColor());
	    this.id = duplicate.getId();
    }
	
	public int getId() {
		return id;
	}
	
	@Override
	public String toString() {
		return "Agent" + id + ";" + color + " - " + location;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Agent) {
			Agent agent = (Agent) obj;
			return this.getId() == agent.getId();
		}
		return false;
	}

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

}
