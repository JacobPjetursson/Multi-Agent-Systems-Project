package task;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import state.Agent;
import state.Box;
import state.Location;
import state.State;
import state.StateObject;

public class MoveAgentTask extends ResolveTask {

	private Set<Location> path;
	private Agent moveAgent;

	public MoveAgentTask(int priority, Task taskToResolve, Agent moveAgent, Collection<Location> path) {
		super(priority, taskToResolve);
		this.moveAgent = moveAgent;
		this.path = new HashSet<>(path);
	}
	
	private Agent getMoveAgent() {
		return moveAgent;
	}
	
	private Set<Location> getPath() {
		return path;
	}

	@Override
	public boolean isTerminal(State state) {
		return !path.contains(state.getAgent(moveAgent).getLocation());
	}

	@Override
	public int h(State state) {
		int h = 0;
		for(Box b : state.getBoxes()) {
			if(path.contains(b.getLocation())) {
				//TODO : 10 chosen randomly maybe not always working
				h+=10;
			}
		}
		return h;
	}
	
	@Override
	public boolean assignAgent(Agent agent) {
		if (agent.getId() == moveAgent.getId()) {
			return super.assignAgent(agent);
		}
		return false;
	}

	@Override
	public boolean updateState(State state) {
		return true;
	}

	@Override
	public void initializeState(State state) {
		//TODO : Possible make another MoveAgent that uses this, to ensure it does not fuck with boxes when moving
		// Maybe update heuristic to avoid moving boxes unless necessary
		//state.setFakeWalls();
	}

	@Override
	public Location getGoalLocation() {
		return null;
	}

	@Override
	public Task getNaive() {
		return new NaiveMoveAgentTask(this);
	}

	@Override
	public Task getNextTask() {
		return null;
	}
	
	private static class NaiveMoveAgentTask extends MoveAgentTask {		
		public NaiveMoveAgentTask(MoveAgentTask task) {
			super(task.getPriority(), task.getTaskToResolve(), task.getMoveAgent(), task.getPath());
		}

		@Override
		public void initializeState(State state) {
			List<StateObject> preserve = new LinkedList<>();
			preserve.add(getAgent());
			state.removeObjectsExcept(preserve);
		}
	}

}
