package task;

import java.util.List;

import state.Box;
import state.Goal;
import state.State;

public class GoalTask extends Task {
	
	private Goal goal;

	public GoalTask(Goal goal) {
		super(3);
		this.goal = goal;
	}
	
	public Goal getGoal() {
		return goal;
	}

	@Override
	public boolean isTerminal(State state) {
		char letter = goal.getLetter();
		List<Box> boxes = state.getBoxes();
		for(Box box : boxes) {
			if (box.getLetter() == letter) {
				if (box.getLocation().equals(goal.getLocation())) {
					return true;
				}
			}
		}
		return false;
	}

}
