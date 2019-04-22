package task;

import java.util.List;

import state.Box;
import state.DistanceMap;
import state.Goal;
import state.State;

public class GoalTask extends Task {
	
	private Goal goal;

	public GoalTask(Goal goal) {
		this(3, goal);
	}
	
	public GoalTask(int priority, Goal goal) {
		super(priority);
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
			if (box.getLetter() == letter && box.getLocation().equals(goal.getLocation()))
				return true;
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
			    // TODO - do not include boxes already in goal
				int val = 0;
				DistanceMap dm = State.DISTANCE_MAPS.get(box.getLocation());
				val += dm.distance(goal.getLocation());
				if (val <= best) {
					best = val;
					goal.assignBox(box);
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

	@Override
	public void initializeState(State state) {
		
	}
	
	@Override 
	public boolean equals(Object o) {
		if (o instanceof GoalTask) {
			GoalTask task = (GoalTask) o;
			return task.goal == this.goal;
		}
		return false;
	}

}
