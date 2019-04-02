package task;

import java.util.List;

import state.*;

public class MoveToBoxTask extends Task {

    private Box box;

    public MoveToBoxTask(Box box) {
        super(5);
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
                DistanceMap dm = State.DISTANCE_MAPS.get(box.getLocation());
                val += dm.distance(agent.getLocation());
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

}
