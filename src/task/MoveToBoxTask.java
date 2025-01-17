package task;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import state.*;

public class MoveToBoxTask extends Task implements BoxTask {

    private Box box;
    private Task nextTask;

    public MoveToBoxTask(Box box, Task nextTask) {
        this(5, box, nextTask);
    }
    
    public MoveToBoxTask(int priority, Box box, Task nextTask) {
    	super(priority);
    	this.box = box;
    	this.nextTask = nextTask;
    }

    public Box getBox() {
        return box;
    }
    
    @Override
    public List<Box> getBoxes() {
    	return Collections.singletonList(getBox());
    }

    @Override
    public int h(State state) {
    	int val = 0;
        DistanceMap dm = State.DISTANCE_MAPS.get(state.getAgent(getAgent()).getLocation());
        val += dm.distance(state.getBox(box).getLocation());
        int dis = 0;
        for(Box box : state.getBoxes()) {
        	dis += State.safeLocation.get(state.getBox(box).getLocation());
			
		}
        return val-dis;
    }
    
    @Override
    public boolean isTerminal(State state) {
    	if (box.getColor() == getAgent().getColor() && box.isNeighbor(state.getAgent(getAgent()).getLocation()))
            return true;
        return false;
    }

	@Override
	public boolean updateState(State state) {
		return true;
	}

	@Override
	public void initializeState(State state) {
		
	}

	@Override
	public Location getGoalLocation() {
		return box.getLocation();
	}

	@Override
	public Task getNaive() {
		return new NaiveMoveToBoxTask(this);
	}

	@Override
	public Task getNextTask() {
		return nextTask;
	}
	
	@Override
	public boolean assignAgent(Agent agent) {
		DistanceMap dm = State.DISTANCE_MAPS.get(agent.getLocation());
		if(dm.distance(box.getLocation()) <= 0) {
			return false;
		}
		return super.assignAgent(agent);
	}
	
	private static class NaiveMoveToBoxTask extends MoveToBoxTask {		
		public NaiveMoveToBoxTask(MoveToBoxTask task) {
			super(task.getPriority(), task.getBox(), task.getNextTask());
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
	public String toString() {
		return ("MoveToBoxTask = Box : " + box.getLetter() + " - Box location : " + box.getLocation() + " - Agent : " + (getAgent() == null ? "null" : getAgent().getId()));
	}

	@Override
	public int estimatedTime(State state) {
		DistanceMap dm = State.DISTANCE_MAPS.get(state.getAgent(getAgent()).getLocation());
		int r = dm.distance(state.getBox(box).getLocation());
		if(nextTask != null) {
			return r + nextTask.estimatedTime(state);
		}
		return r;
	}

}
