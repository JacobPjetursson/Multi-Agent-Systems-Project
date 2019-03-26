package action;

import state.Agent;
import state.Box;
import state.State;

public class PullAction extends Action {
    private Agent agent;
    private Box box;

    public PullAction(Dir dir1, Dir dir2) {
        this.dir1 = dir1;
        this.dir2 = dir2;
    }

    public Agent getAgent() {
        return agent;
    }

    public Box getBox() {
        return box;
    }

    public String toString() {
        return String.format("Pull(%s,%s);", dir1, dir2);
    }

    @Override
    public void apply(State state) {
        for (Agent a : state.getAgents()) { // TODO - make hashset instead for constant lookup of agents
            if (a.equals(agent))
                a.move(dir1);
        }
        for (Box b : state.getBoxes()) {
            if (b.equals(box))
                b.move(dir2);
        }
    }
}
