package task;

import java.util.Collection;

import state.Box;
import state.DistanceMap;
import state.Location;
import state.State;

public class MoveBoxTask extends Task {
	
	private Collection<Location> path;
	private Box box;

	public MoveBoxTask(int priority, Box box, Collection<Location> path) {
		super(priority);
		this.box = box;
		this.path = path;
	}

	@Override
	public boolean isTerminal(State state) {
		//return !path.contains(state.getBox(box)); // TODO - boxes should have id?
		return true;
	}

	@Override
	public int h(State state) {
		//Box box = state.getBox(box);
		DistanceMap dm = State.DISTANCE_MAPS.get(box.getLocation());
		return dm.distance(getAgent().getLocation());
	}

	@Override
	public boolean updateState(State state) {
		return true;
	}

	@Override
	public void initializeState(State state) {}

}
