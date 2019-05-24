package task;

import state.*;

import java.util.LinkedList;
import java.util.List;

public class SimpleTask extends Task {
    private Location loc;
    public SimpleTask(Location loc, int priority) {
        super(priority);
        this.loc = loc;
    }

    @Override
    public boolean isTerminal(State state) {
        return state.getAgent(getAgent()).getLocation().equals(loc);
    }

    @Override
    public int h(State state) {
        int val = 0;
        DistanceMap dm = State.DISTANCE_MAPS.get(getAgent().getLocation());
        val += dm.distance(loc);
        return val;
    }

    @Override
    public boolean updateState(State state) {
        return true;
    }

    @Override
    public void initializeState(State state) {
        List<StateObject> preserve = new LinkedList<>();
        Agent a = state.getAgent(getAgent());
        preserve.add(a);
        for(Box box : state.getBoxes()) {
            if(box.getColor() != a.getColor()) {
                preserve.add(box);
            }
        }
        state.removeObjectsExcept(preserve);
    }

    @Override
    public Location getGoalLocation() {
        return loc;
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
	public int estimatedTime(State state) {
		DistanceMap dm = State.DISTANCE_MAPS.get(state.getAgent(getAgent()).getLocation());
        return dm.distance(loc);
	}
}
