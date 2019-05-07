package task;

import java.util.Collection;
import java.util.Collections;
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

public class MoveBoxTask extends ResolveTask implements BoxTask {
	
	private Set<Location> path;
	private Box box;

	public MoveBoxTask(int priority, Task taskToResolve, Box box, Collection<Location> path) {
		super(priority, taskToResolve);
		this.box = box;
		this.path = new HashSet<>(path);
	}
	
	private Box getBox() {
		return box;
	}
	
	@Override
	public List<Box> getBoxes() {
		return Collections.singletonList(getBox());
	}
	
	private Set<Location> getPath(){
		return path;
	}

	@Override
	public boolean isTerminal(State state) {
		return !path.contains(state.getBox(box).getLocation());
	}

	@Override
	public int h(State state) {
		int h = 0;
		DistanceMap dm = State.DISTANCE_MAPS.get(state.getBox(box).getLocation());
		for(Box box : state.getBoxes()) {
			if(path.contains(state.getBox(box).getLocation())) {
				h+=10;
			}
		}
		return h+dm.distance(state.getAgent(getAgent()).getLocation());
	}

	@Override
	public boolean updateState(State state) {
		return true;
	}

	@Override
	public void initializeState(State state) {
		//state.setFakeWalls(box);
	}

	@Override
	public Task getNaive() {
		return new NaiveMoveBoxTask(this);
	}

	@Override
	public Task getNextTask() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public boolean assignAgent(Agent agent) {
		DistanceMap dm = State.DISTANCE_MAPS.get(agent.getLocation());
		if(dm.distance(box.getLocation()) <= 0) {
			return false;
		}
		return super.assignAgent(agent);
	}
	
	private static class NaiveMoveBoxTask extends MoveBoxTask {		
		public NaiveMoveBoxTask(MoveBoxTask task) {
			super(task.getPriority(), task.getTaskToResolve(), task.getBox(), task.getPath());
		}

		@Override
		public void initializeState(State state) {
			List<StateObject> preserve = new LinkedList<>();
			preserve.add(getAgent());
			//preserve.add(super.getBox());
			for(Box box: state.getBoxes()) {
				if(box.getColor() == getAgent().getColor()) {
					preserve.add(box);
				}
			}
			state.removeObjectsExcept(preserve);
		}
	}
	
	
	public String toString() {
		return ("MoveBoxTask = Box : " + box.getLetter() + " - Box location : " + box.getLocation() + " - Agent : " + super.getAgent().getId());
	}

}
