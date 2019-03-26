package action;

import state.Agent;

public class MoveAction extends Action{
    Agent agent;

    public MoveAction(Agent agent, Dir dir1) {
        this.agent = agent;
        this.dir1 = dir1;
    }

    public Agent getAgent() {
        return agent;
    }

    public String toString() {
        return String.format("Move(%s);", dir1);
    }
}
