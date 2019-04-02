package task;

import java.util.List;

import state.Box;
import state.DistanceMap;
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

	@Override
	public int h(State state) {
		char letter = goal.getLetter();
		List<Box> boxes = state.getBoxes();
		int best = Integer.MAX_VALUE;
		for(Box box : boxes) {
			if (box.getLetter() == letter) {
				int val = 0;
				DistanceMap dm = State.DISTANCE_MAPS.get(box.getLocation());
				val += dm.distance(getAgent().getLocation());
				val += dm.distance(goal.getLocation());
				if (val <= best) {
					best = val;
				}
			}
		}
		return best;
	}
	
	@Override
	public boolean updateState(State state) {
		// The world is assumed to be static
		return true;
	}

}
