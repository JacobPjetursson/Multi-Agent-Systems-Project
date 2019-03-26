package state;

import action.Action;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class State{

	public static boolean[][] walls;
	public static List<Goal> goals;
	private List<Box> boxes;
	private List<Agent> agents;


	public State(List<Agent> agents, List<Box> boxes) {
	    this.agents = agents;
	    this.boxes = boxes;
	}
	
	public List<Agent> getAgents() {
		return agents;
	}
	
	public List<Goal> getGoals() {
		return goals;
	}

	public State(State parent, Action action) {
	    this.agents = new ArrayList<>();
	    this.boxes = new ArrayList<>();

	    for (Agent a : parent.getAgents())
	        this.agents.add(new Agent(a));
	    for (Box b : parent.getBoxes())
	        this.boxes.add(new Box(b));

	    action.apply(this);
    }

    public List<Agent> getAgents() {
	    return agents;
    }

    public List<Box> getBoxes() {
	    return boxes;
    }

    public boolean isTerminal() {
	    return false; // TODO
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof State)) return false;
        State state = (State) o;
        return Objects.equals(boxes, state.boxes) &&
                Objects.equals(agents, state.agents);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boxes, agents);
    }

    public int g() {
	    return 0;
    }

    // Get children of state where only agent moves
    public LinkedList<State> getChildren(Agent agent) {
	    return null; // TODO
    }

    public List<Action> extractPlan() {
	    return null; // TODO
    }
}
