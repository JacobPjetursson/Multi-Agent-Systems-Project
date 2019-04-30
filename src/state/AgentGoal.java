package state;

public class AgentGoal extends Goal {

    private Agent assignedAgent;

    public AgentGoal(Location position, int color, char letter) {
        super(position, color, letter);
    }


    @Override
    public void assignObj(MovableObject obj) {
        assignedAgent = (Agent) obj;
    }

    @Override
    public MovableObject getAssignedObj() {
        return assignedAgent;
    }
}
