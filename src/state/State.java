package state;

import action.Action;

import java.util.*;

public class State{

	public static boolean[][] walls;
	public static List<Goal> goals;
	private List<Box> boxes;
	private List<Agent> agents;
	private State parent;


	// Initial state
	public State(List<Agent> agents, List<Box> boxes) {
	    this.agents = agents;
	    this.boxes = boxes;
	    this.parent = null;

	}

	// Intermediate state
	public State(State parent, Action action) {
	    this.agents = new ArrayList<>();
	    this.boxes = new ArrayList<>();
	    this.parent = parent;

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

    public ArrayList<State> extractPlan() {
        ArrayList<State> plan = new ArrayList<>();
        State n = this;
        while (n.parent != null) {
            plan.add(n);
            n = n.parent;
        }
        Collections.reverse(plan);
        return plan;
    }
}
