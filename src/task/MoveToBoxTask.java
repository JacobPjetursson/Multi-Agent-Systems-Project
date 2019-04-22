package task;

import java.util.List;

import state.*;

public class MoveToBoxTask extends Task {

    private Box box;

    public MoveToBoxTask(Box box) {
        this(5,box);
    }
    
    public MoveToBoxTask(int priority, Box box) {
    	super(priority);
    	this.box = box;
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
        return best;
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

}
