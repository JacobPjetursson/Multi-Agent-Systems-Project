package action;

import state.Agent;
import state.State;

public class MoveAction extends Action{
    Agent agent;

    public MoveAction(Dir dir1) {

        this.dir1 = dir1;
    }

    public Agent getAgent() {
        return agent;
    }

    public String toString() {
        return String.format("Move(%s);", dir1);
    }

    @Override
    public void apply(State state) {
        for (Agent a : state.getAgents()) { // TODO - make hashset instead for constant lookup of agents
            if (a.equals(agent))
                a.move(dir1);
        }
    }
}
