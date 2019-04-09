package task;

import state.Goal;
import state.State;

public class NaiveGoalTask extends GoalTask {

	public NaiveGoalTask(int priority, Goal goal) {
		super(priority,goal);
	}

	@Override
	public boolean isTerminal(State state) {
		return super.isTerminal(state);
	}

	@Override
	public int h(State state) {
		return super.h(state);
	}

	@Override
	public boolean updateState(State state) {
		return super.updateState(state);
	}

	@Override
	public void initializeState(State state) {
		// TODO - Remove all except specific box
		state.removeObjectsExcept(getAgent(), getAgent().getColor());
	}

}
