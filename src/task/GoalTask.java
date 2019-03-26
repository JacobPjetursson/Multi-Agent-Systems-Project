package task;

import state.Goal;

public class GoalTask extends Task {
	
	private Goal goal;

	public GoalTask(Goal goal) {
		super(3);
		this.goal = goal;
	}
	
	public Goal getGoal() {
		return goal;
	}

}
