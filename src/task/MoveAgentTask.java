package task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import state.Agent;
import state.Box;
import state.DistanceMap;
import state.Location;
import state.State;

public class MoveAgentTask extends ResolveTask {

	private Set<Location> path;
	private Agent agent;

	public MoveAgentTask(int priority, Task taskToResolve, Agent agent, Collection<Location> path) {
		super(priority, taskToResolve);
		this.agent = agent;
		this.path = new HashSet<>(path);
	}

	@Override
	public boolean isTerminal(State state) {
		return !path.contains(state.getAgent(agent).getLocation());
	}

	@Override
	public int h(State state) {
		//Box box = state.getBox(box);
		DistanceMap dm = State.DISTANCE_MAPS.get(agent.getLocation());
		return dm.distance(getAgent().getLocation());
	}

	@Override
	public boolean updateState(State state) {
		return true;
	}

	@Override
	public void initializeState(State state) {}

}
