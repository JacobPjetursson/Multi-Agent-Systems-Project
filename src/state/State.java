package state;

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
}
