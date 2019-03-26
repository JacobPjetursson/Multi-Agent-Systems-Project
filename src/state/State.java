package state;

import action.Action;
import action.MoveAction;
import task.Task;

import java.util.*;

public class State {
	
	public static final Map<Location, DistanceMap> DISTANCE_MAPS = new HashMap<>();
	
	public static int ROWS;
	public static int COLS;
	public static boolean[][] walls;
	public static List<Goal> goals;
	
	private List<Box> boxes;
	private List<Agent> agents;
	private State parent;
	private Action action;
	private int g;


	// Initial state
	public State(List<Agent> agents, List<Box> boxes) {
		this.agents = agents;
		this.boxes = boxes;
		this.parent = null;
		this.action = null;
		g = 0;
		
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				Location l = new Location(i, j);
				DISTANCE_MAPS.put(l, new DistanceMap(l));
			}
		}
	}

	public List<Agent> getAgents() {
		return agents;
	}

	public List<Goal> getGoals() {
		return goals;
	}

	public List<Box> getBoxes() {
		return boxes;
	}

	// Intermediate state
	public State(State parent, Action action) {
		this.agents = new ArrayList<>();
		this.boxes = new ArrayList<>();
		this.action = action;
		this.parent = parent;
		g++;

		for (Agent a : parent.getAgents())
			this.agents.add(new Agent(a));
		for (Box b : parent.getBoxes())
			this.boxes.add(new Box(b));

		action.apply(this);
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

	public int f(Task task) {
		return g + task.h(this);
	}

	// Get children of state where only agent moves
	public LinkedList<State> getChildren(Agent agent) {
		LinkedList<State> children = new LinkedList<>();
		for (Action a : getLegalActions(agent))
			children.add(new State(this, a));
		return children;
	}

	private LinkedList<Action> getLegalActions(Agent agent) {
		LinkedList<Action> legalActions = new LinkedList<>();
		for (Agent a : this.agents) {
			if (agent.equals(a)) {
				Location loc = a.getLocation();
				if (!walls[loc.getRow() - 1][loc.getCol()])
					legalActions.add(new MoveAction(a, Action.Dir.N));
				if (!walls[loc.getRow() + 1][loc.getCol()])
					legalActions.add(new MoveAction(a, Action.Dir.S));
				if (!walls[loc.getRow()][loc.getCol() + 1])
					legalActions.add(new MoveAction(a, Action.Dir.E));
				if (!walls[loc.getRow() - 1][loc.getCol() - 1])
					legalActions.add(new MoveAction(a, Action.Dir.W));
			}
		}
		return legalActions;
	}

	public ArrayList<Action> extractPlan() {
		ArrayList<Action> plan = new ArrayList<>();
		State n = this;
		while (n.parent != null) {
			plan.add(n.action);
			n = n.parent;
		}
		Collections.reverse(plan);
		return plan;
	}
}
