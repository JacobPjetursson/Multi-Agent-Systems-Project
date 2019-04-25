package task;

import java.util.LinkedList;
import java.util.List;

import state.*;

public class MoveToBoxTask extends Task {

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
    public int h(State state) {

        int color = box.getColor();
        List<Agent> agents = state.getAgents();
        int best = Integer.MAX_VALUE;
        for(Agent agent : agents) {
            if (agent.getColor() == color) {
                int val = 0;
                DistanceMap dm = State.DISTANCE_MAPS.get(agent.getLocation());
                val += dm.distance(box.getLocation());
                if (val <= best) {
                    best = val;
                }
            }
        }
        int dis = 0;
		for(Box box : state.getBoxes()) {
			if(!this.box.equals(box)) {
				dis += State.safeLocation.get(box.getLocation());
			}
			
		}
        return best-dis;
    }
    
    @Override
    public boolean isTerminal(State state) {
    	List<Agent> agents = state.getAgents();
        for(Agent agent : agents) {
            if (box.getColor() == agent.getColor() && box.isNeighbor(agent.getLocation()))
                return true;
        }
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
	public Task getNaive() {
		return new NaiveMoveToBoxTask(this);
	}

	@Override
	public Task getNextTask() {
		return nextTask;
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
		return ("MoveToBoxTask = Box : " + box.getLetter() + " - Box location : " + box.getLocation() + " - Agent : " + super.getAgent().getId());
	}

}
