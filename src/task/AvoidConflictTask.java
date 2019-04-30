package task;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import action.Action;
import state.State;

public class AvoidConflictTask extends Task {
	
	private List<Action> avoidPlan;
	private int avoidAgentId;

	public AvoidConflictTask(int priority, int agentId, Collection<Action> plan) {
		super(priority);
		this.avoidPlan = new ArrayList<>(plan);
		this.avoidAgentId = agentId;
	}

	@Override
	public boolean isTerminal(State state) {
		// TODO - This is not a terminal...
		return state.g() >= avoidPlan.size();
	}

	@Override
	public int h(State state) {
		// TODO - Maybe something about how many spaces on the path are filled
		return 0;
	}
	
	@Override
	public boolean updateState(State state) {
		int index = state.g() - 1;
		Action action = avoidPlan.get(index);
		return state.applyAction(state.getAgent(avoidAgentId), action);
	}

	@Override
	public void initializeState(State state) {
		
		
	}

	@Override
	public Task getNaive() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Task getNextTask() {
		return null;
	}

}
