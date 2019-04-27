package task;

import java.util.LinkedList;
import java.util.List;

import state.Box;
import state.DistanceMap;
import state.Goal;
import state.State;
import state.StateObject;

public class GoalTask extends Task {
	
	private Box box;
	private Goal goal;

	public GoalTask(Box box, Goal goal) {
		this(3, box, goal);
	}
	
	public GoalTask(int priority, Box box, Goal goal) {
		super(priority);
		this.goal = goal;
		this.box = box;
	}
	
	public Goal getGoal() {
		return goal;
	}
	
	public Box getBox() {
		return box;
	}

	@Override
	public boolean isTerminal(State state) {
		Box box = state.getBox(this.box);
		return box.getLocation().equals(goal.getLocation());
	}

	@Override
	public int h(State state) {
		DistanceMap dm = State.DISTANCE_MAPS.get(state.getBox(box).getLocation());
		int val = dm.distance(goal.getLocation());
		
		int dis = 0;
		for(Box box : state.getBoxes()) {
			if(!State.goalMap.containsKey(box.getLocation())) {
				dis += State.safeLocation.get(box.getLocation());
			}
			
		}
        return val-dis;
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

	@Override
	public Task getNaive() {
		return new NaiveGoalTask(this);
	}

	@Override
	public Task getNextTask() {
		return null;
	}
	
	private static class NaiveGoalTask extends GoalTask {		
		public NaiveGoalTask(GoalTask task) {
			super(task.getPriority(), task.getBox(), task.getGoal());
		}

		@Override
		public void initializeState(State state) {
			List<StateObject> preserve = new LinkedList<>();
			preserve.add(getAgent());
			preserve.add(getBox());
			state.removeObjectsExcept(preserve);
		}
	}
}
