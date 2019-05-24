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
import task.Task;

public class MoveBoxesAndAgentTask extends ResolveTask implements BoxTask {
	
	private Set<Location> path;
	private List<Box> boxes;
	private Agent agent;

	public MoveBoxesAndAgentTask(int priority, Task taskToResolve, List<Box> boxes, Agent agent, Collection<Location> path) {
		super(priority, taskToResolve);
		this.boxes = boxes;
		this.path = new HashSet<>(path);
		this.agent = agent;
	}
	
	@Override
	public List<Box> getBoxes() {
		return boxes;
	}
	
	private Set<Location> getPath(){
		return path;
	}
	
	public Agent getAgent(){
		return agent;
	}

	@Override
	public boolean isTerminal(State state) {
		for(Box box : boxes) {
			Box b = state.getBox(box);
			if (b == null) continue;
			if(path.contains(b.getLocation())) {
				return false;
			}
		}
		return !path.contains(state.getAgent(agent).getLocation());
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
			Box b = state.getBox(box);
			if (b == null) continue;
			dis += State.safeLocation.get(b.getLocation());
		}
		return h-dis;
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
	public Location getGoalLocation() {
		return null;
	}

	@Override
	public Task getNaive() {
		return new NaiveMoveBoxesAndAgentTask(this);
		//return null;
	}

	@Override
	public Task getNextTask() {
		return null;
	}
	
	@Override
	public boolean assignAgent(Agent agent) {
		if(agent.equals(this.agent)) {
			return super.assignAgent(agent);
		}
		return false;
	}
	
	private static class NaiveMoveBoxesAndAgentTask extends MoveBoxesAndAgentTask {		
		public NaiveMoveBoxesAndAgentTask(MoveBoxesAndAgentTask task) {
			super(task.getPriority(), task.getTaskToResolve(), task.getBoxes(), task.getAgent(), task.getPath());
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
		return ("MoveBoxesAndAgentTask");
	}

	@Override
	public int estimatedTime(State state) {
		//Not correct
		int maxDist = 0;
		DistanceMap dm = State.DISTANCE_MAPS.get(state.getAgent(agent).getLocation());
		for(Location loc : path) {
			int dist = dm.distance(loc) + 1;
			if(dist > maxDist) {
				maxDist = dist;
			}
		}
		for(Box box : boxes) {
			int max = 0;
			Box b = state.getBox(box);
			if (b == null)
			    continue;
			dm = State.DISTANCE_MAPS.get(state.getBox(box).getLocation());
			for(Location loc : path) {
				int dist = dm.distance(loc) + 1;
				if(dist > max) {
					max = dist;
				}
			}
			maxDist+=max;
		}
		return maxDist*2;
	}

}
