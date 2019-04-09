package task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import state.Box;
import state.DistanceMap;
import state.Location;
import state.State;

public class MoveBoxTask extends ResolveTask {
	
	private Set<Location> path;
	private Box box;

	public MoveBoxTask(int priority, Task taskToResolve, Box box, Collection<Location> path) {
		super(priority, taskToResolve);
		this.box = box;
		this.path = new HashSet<>(path);
	}

	@Override
	public boolean isTerminal(State state) {
		return !path.contains(state.getBox(box).getLocation());
	}

	@Override
	public int h(State state) {
		int h = 0;
		DistanceMap dm = State.DISTANCE_MAPS.get(box.getLocation());
		for(Box b : state.getBoxes()) {
			if(path.contains(b.getLocation())) {
				h+=10;
			}
		}
		return h+dm.distance(getAgent().getLocation());
	}

	@Override
	public boolean updateState(State state) {
		return true;
	}

	@Override
	public void initializeState(State state) {
		//state.setFakeWalls(box);
	}

}
