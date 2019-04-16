package task;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import state.Agent;
import state.Box;
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
	public boolean updateState(State state) {
		return true;
	}

	@Override
	public void initializeState(State state) {
		//TODO : Possible make another MoveAgent that uses this, to ensure it does not fuck with boxes when moving
		//state.setFakeWalls();
	}

}
