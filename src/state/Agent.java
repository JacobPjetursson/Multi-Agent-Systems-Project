package state;

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
	public Agent move(Location position) {
		return new Agent(position, color, id);
	}
	
	@Override
	public String toString() {
		return "Agent" + id + ";" + color + " - " + position;
	}

}
