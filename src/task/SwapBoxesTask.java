package task;
import state.Box;
import state.DistanceMap;
import state.Location;
import state.State;
import task.Task;

public class SwapBoxesTask extends Task {
	
	private Box box1;
	private Box box2;

	public SwapBoxesTask(int priority, Box box1, Box box2) {
		super(priority);
		this.box1 = new Box(box1);
		this.box2 = new Box(box2);
		
	}

	@Override
	public boolean isTerminal(State state) {
		return ((state.getBox(box2).getLocation()).equals(box1.getLocation())) && ((state.getBox(box1).getLocation()).equals(box2.getLocation()));
	}

	@Override
	public int h(State state) {
		DistanceMap dm1 = State.DISTANCE_MAPS.get(box1.getLocation());
		DistanceMap dm2 = State.DISTANCE_MAPS.get(box2.getLocation());
		return dm1.distance(state.getBox(box2).getLocation()) + dm2.distance(state.getBox(box1).getLocation());
	}

	@Override
	public boolean updateState(State state) {
		return true;
	}

	@Override
	public void initializeState(State state) {

	}

	@Override
	public Task getNaive() {
		return null;
	}

	@Override
	public Task getNextTask() {
		return null;
	}

	@Override
	public Location getGoalLocation() {
		return null;
	}

	@Override
	public int estimatedTime(State state) {
		DistanceMap dm = State.DISTANCE_MAPS.get(state.getBox(box1).getLocation());
		return dm.distance(state.getBox(box2).getLocation())*3;
	}

}
