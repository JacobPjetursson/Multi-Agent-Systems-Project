package task;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import state.Box;
import state.DistanceMap;
import state.Goal;
import state.State;
import state.StateObject;
import state.Location;

public class GoalTask extends Task implements BoxTask {

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
	public List<Box> getBoxes() {
		return Collections.singletonList(getBox());
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
		return val;
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
	public Location getGoalLocation() {
		return goal.getLocation();
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

	@Override
	public int estimatedTime(State state) {
		DistanceMap dm = State.DISTANCE_MAPS.get(state.getBox(box).getLocation());
		return dm.distance(goal.getLocation());
	}
}
