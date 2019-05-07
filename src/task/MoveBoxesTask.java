package task;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import state.Agent;
import state.Box;
import state.DistanceMap;
import state.Location;
import state.State;
import state.StateObject;

public class MoveBoxesTask extends ResolveTask implements BoxTask {
	
	private Set<Location> path;
	private List<Box> boxes;

	public MoveBoxesTask(int priority, Task taskToResolve, List<Box> boxes, Collection<Location> path) {
		super(priority, taskToResolve);
		this.boxes = boxes;
		this.path = new HashSet<>(path);
	}
	
	@Override
	public List<Box> getBoxes() {
		return boxes;
	}
	
	private Set<Location> getPath(){
		return path;
	}

	@Override
	public boolean isTerminal(State state) {
		for(Box box : boxes) {
			if(path.contains(state.getBox(box).getLocation())) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int h(State state) {
		int h = 0;
		for(Box box : state.getBoxes()) {
			if(path.contains(state.getBox(box).getLocation())) {
				h+=5;
			}
		}
		int dis = 0;
		for(Box box : boxes) {
			dis += State.safeLocation.get(state.getBox(box).getLocation());
		}
		return h-dis;
	}

	@Override
	public boolean updateState(State state) {
		return true;
	}

	@Override
	public void initializeState(State state) {
	}

	@Override
	public Task getNaive() {
		return new NaiveMoveBoxesTask(this);
	}

	@Override
	public Task getNextTask() {
		return null;
	}
	
	@Override
	public boolean assignAgent(Agent agent) {
		DistanceMap dm = State.DISTANCE_MAPS.get(agent.getLocation());
		for(Box box : boxes) {
			if(dm.distance(box.getLocation()) <= 0) {
				return false;
			}
		}
		return super.assignAgent(agent);
	}
	
	private static class NaiveMoveBoxesTask extends MoveBoxesTask {		
		public NaiveMoveBoxesTask(MoveBoxesTask task) {
			super(task.getPriority(), task.getTaskToResolve(), task.getBoxes(), task.getPath());
		}

		@Override
		public void initializeState(State state) {
			List<StateObject> preserve = new LinkedList<>();
			preserve.add(getAgent());
			for(Box box: state.getBoxes()) {
				if(box.getColor() == getAgent().getColor()) {
					preserve.add(box);
				}
			}
			state.removeObjectsExcept(preserve);
		}
	}
	
	
	public String toString() {
		return ("MoveBoxesTask");
	}

}
