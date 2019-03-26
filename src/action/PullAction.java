package action;

import state.Agent;
import state.Box;

public class PullAction extends Action {
    private Agent agent;
    private Box box;

    public PullAction(Agent agent, Box box, Dir dir1, Dir dir2) {
        this.agent = agent;
        this.box = box;
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
}
