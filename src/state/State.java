package state;

import action.Action;
import action.MoveAction;
import action.PullAction;
import action.PushAction;
import task.Task;

import java.util.*;
import java.util.stream.Collectors;

public class State{
	public static final Map<Location, DistanceMap> DISTANCE_MAPS = new HashMap<>();
	public static int ROWS;
	public static int COLS;

	public static boolean[][] walls;
	public static List<Goal> goals;

	private List<Box> boxes;
	private HashMap<Integer, Agent> agents;
	private State parent;
	private Action action;
	private int g;

	// Initial state
	public State(HashMap<Integer, Agent> agents, List<Box> boxes) {
		this.agents = agents;
		this.boxes = boxes;
		this.parent = null;
		this.action = null;
		g = 0;

		// Preprocess distance maps
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				Location l = new Location(i, j);
				DISTANCE_MAPS.put(l, new DistanceMap(l));
			}
		}
	}

	// Intermediate state
	private State(State parent, Agent agent, Action action) {
		this(parent);
		this.action = null;
		if (applyAction(agent, action)) {
			this.action = action;
		}
		this.parent = parent;
		g++;
	}

	// Copy
	private State(State state) {
		this.agents = copyAgents(state.agents);
		this.boxes = copyBoxes(state.boxes);
		this.action = state.action;
		this.parent = state.parent;
		this.g = state.g;
	}

	public List<Agent> getAgents() {
		return agents.values().stream().collect(Collectors.toList());
	}

	public List<Goal> getGoals() {
		return goals;
	}

	public List<Box> getBoxes() {
		return boxes;
	}
	
	public Agent getAgent(Agent agent) {
		return agents.get(agent.getId());
	}

	public Agent getAgent(int id) {
		return agents.get(id);
	}

	public Agent getAgentAt(Location location) {
		for (Agent agent : agents.values()) {
			if (location.equals(agent.location)) {
				return agent;
			}
		}
		return null;
	}

	public Box getBoxAt(Location location) {
		for (Box box : getBoxes()) {
			if (location.equals(box.location)) {
				return box;
			}
		}
		return null;
	}
	
	public MovableObject getObjectAt(Location location) {
		MovableObject object = getAgentAt(location);
		object = object == null ? getBoxAt(location) : object;
		return object;
	}

	private HashMap<Integer, Agent> copyAgents(HashMap<Integer, Agent> old) {
	    HashMap<Integer, Agent> copy = new HashMap<>();
		for (Agent a : old.values())
		    copy.put(a.getId(), new Agent(a));

        return copy;
	}

	private List<Box> copyBoxes(List<Box> old) {
		return old.stream().map(x -> new Box(x)).collect(Collectors.toList());
	}

	@Override
	protected State clone() {
		return new State(this);
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
	
	public int g() {
		return g;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Boxes: ");
		for (Box b : boxes)
			sb.append("(").append(b.toString()).append("), ");
		sb.append("\nAgents: ");
		for (Agent a : agents.values())
			sb.append("(").append(a.toString()).append("), ");
		sb.append("\n");
		return sb.toString();
	}

	private State expand(Agent agent, Action action) {
		State state = new State(this, agent, action);
		// Check if action is applied
		return state.action == null ? null : state;
	}

	public boolean applyAction(Agent agent, Action action) {
		Location agentLocation = agent.getLocation();
		if (action instanceof MoveAction) {
			// Check if there's a wall or box on the cell to which the agent is moving
			MoveAction moveAction = (MoveAction) action;
			Location newLocation = agentLocation.move(moveAction.getDirection());
			if (this.cellIsFree(newLocation)) {
				getAgent(agent).setLocation(newLocation);
				return true;
			}
		} else if (action instanceof PushAction) {
			// Make sure that there's actually a box to move
			PushAction pushAction = (PushAction) action;
			Location newAgentLocation = agentLocation.move(pushAction.getAgentDirection());
			Box box = getBoxAt(newAgentLocation);
			if (box != null && box.getColor() == agent.getColor()) {
				Location newBoxLocation = box.getLocation().move(pushAction.getBoxDirection());
				// .. and that new cell of box is free
				if (this.cellIsFree(newBoxLocation)) {
					getAgent(agent).setLocation(newAgentLocation);
					box.setLocation(newBoxLocation);
					return true;
				}
			}
		} else if (action instanceof PullAction) {
			// Cell is free where agent is going
			PullAction pullAction = (PullAction) action;
			Location newAgentLocation = agentLocation.move(pullAction.getAgentDirection());
			if (this.cellIsFree(newAgentLocation)) {
				Location boxLocation = agentLocation.move(pullAction.getBoxDirection());
				Box box = getBoxAt(boxLocation);
				// .. and there's a box in "dir2" of the agent
				if (box != null && box.getColor() == agent.getColor()) {
					getAgent(agent).setLocation(newAgentLocation);
					box.setLocation(agentLocation);
					return true;
				}
			}
		}
		return false;
	}

	public ArrayList<State> getExpandedStates(int agentId) {
		ArrayList<State> expandedStates = new ArrayList<>(Action.EVERY.length);
		Agent agent = getAgent(agentId);

		for (Action action : Action.EVERY) {
			State child = expand(agent, action);
			if (child != null) {
				expandedStates.add(child);
			}
		}
		return expandedStates;
	}

	private boolean cellIsFree(int row, int col) {
		boolean boxFree = true;
		for (Box b : boxes) { // TODO - optimize
			Location boxLoc = b.getLocation();
			if (boxLoc.getRow() == row && boxLoc.getCol() == col)
				boxFree = false;
		}
		return !walls[row][col] && boxFree;
	}
	
	private boolean cellIsFree(Location location) {
		return cellIsFree(location.getRow(), location.getCol());
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
