package task;

import state.State;

public class PlanTask extends Task {

	public PlanTask(int priority) {
		super(priority);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected boolean isTerminal(State state) {
		// TODO Auto-generated method stub
		return false;
	}

}
