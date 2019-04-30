package state;

import action.Action;
import action.BoxAction;
import action.MoveAction;
import action.PullAction;
import action.PushAction;
import task.Task;

import java.util.*;

public class State{
	public static final Map<Location, DistanceMap> DISTANCE_MAPS = new HashMap<>();
	public static int ROWS;
	public static int COLS;

	public static boolean[][] walls;
	public static List<Goal> goals;
	public static Map<Location,Goal> goalMap;
	public static Map<Location, Integer> safeLocation;
	public static int totalGoals;
	public static int freeBoxes;

	private Set<Location> fakeWalls;
	private Map<Integer, Agent> agents;
	private Map<Integer, Box> boxes;
	private State parent;
	private Action action;
	private int g;

	// Initial state
	public State(Map<Integer, Agent> agents, Map<Integer, Box> boxes) {
		this.agents = agents;
		this.boxes = boxes;
		this.parent = null;
		this.action = null;
		this.fakeWalls = new HashSet<>();
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
		this.fakeWalls = copyFakeWalls(state.fakeWalls);
		this.g = state.g;
	}

	// Naively assign boxes to goals based on distance
	public void assignObjectsToGoals() {
		Set<MovableObject> assigned = new HashSet<>();
	    for (Goal g : goals) {
	    	if (g instanceof BoxGoal) {
				int best = Integer.MAX_VALUE;
				for(Box box : boxes.values()) {
					if (box.getLetter() == g.getLetter() && !assigned.contains(box)) {
						int val = 0;
						DistanceMap dm = State.DISTANCE_MAPS.get(box.getLocation());
						val += dm.distance(g.getLocation());
						if (val <= best && val>0) {
							best = val;
							g.assignObj(box);
						}
					}
				}
			} else {
	    		for (Agent agent : agents.values()) {
	    			if (agent.getLetter() == g.getLetter())
	    				g.assignObj(agent);
				}
			}
            assigned.add(g.getAssignedObj());
        }
    }

    public List<MovableObject> getAssignedObjects() {
	    ArrayList<MovableObject> assignedObjects = new ArrayList<>();
	    for (Goal g : goals) {
	        MovableObject obj = g.getAssignedObj();
	        if (obj != null)
	            assignedObjects.add(obj);
        }
        return assignedObjects;
    }

	public List<Agent> getAgents() {
		return new ArrayList<>(agents.values());
	}

	public List<Box> getBoxes() {
		return new ArrayList<>(boxes.values());
	}

	/*
	public void removeObjectsExcept(Agent agent, int color) {
		agents = new HashMap<>();
		agents.put(agent.getId(), agent);
		Map<Integer, Box> newBoxes = new HashMap<>();
		for(Box b : getBoxes()) {
			if (b.getColor() == color) {
				newBoxes.put(b.getId(), b);
			}
		}
		boxes = newBoxes;
	}
	*/
	
	public void removeObjectsExcept(List<StateObject> preserve) {
		agents = new HashMap<>();
		boxes = new HashMap<>();
		for (StateObject object : preserve) {
			if (object instanceof Agent) {
				Agent agent = (Agent) object;
				agents.put(agent.getId(), agent);
			}
			else if (object instanceof Box) {
				Box box = (Box) object;
				boxes.put(box.getId(), box);
			}
		}
	}

	public List<Goal> getGoals() {
		return goals;
	}
	/*
	public List<Goal> getAgentGoals() {
		return agentGoals;
	}
*/
	public Agent getAgent(Agent agent) {
		return getAgent(agent.getId());
	}

	public Agent getAgent(int id) {
		return agents.get(id);
	}

	public Box getBox(Box box) {
		return getBox(box.getId());
	}

	public Box getBox(int id) {
		return boxes.get(id);
	}

	public Agent getAgentAt(Location location) {
		// TODO - optimize
		for (Agent agent : getAgents()) {
			if (location.equals(agent.location)) {
				return agent;
			}
		}
		return null;
	}

	public Box getBoxAt(Location location) {
		// TODO - optimize
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

	private Map<Integer, Agent> copyAgents(Map<Integer, Agent> old) {
		HashMap<Integer, Agent> copy = new HashMap<>();
		for (Agent a : old.values())
			copy.put(a.getId(), new Agent(a));
		return copy;
	}

	private Map<Integer, Box> copyBoxes(Map<Integer, Box> old) {
		HashMap<Integer, Box> copy = new HashMap<>();
		for (Box b : old.values()) {
			copy.put(b.getId(), new Box(b));
		}
		return copy;
	}
	
	private Set<Location> copyFakeWalls(Set<Location> old) {
		return new HashSet<>(old);
	}
	
	public void setFakeWalls() {
		for(Box box : getBoxes()) {
			fakeWalls.add(box.location);
		}
	}
	
	public void setFakeWalls(Box box) {
		setFakeWalls();
		fakeWalls.remove(box.location);
	}

	@Override
	public State clone() {
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
		//TODO : Remove boxes in goal when it stops moving shit
		return g + task.h(this) - boxesInGoal();
	}
	
	public int boxesInGoal() {
		int goalCount = 0;
		for(Box box : this.getBoxes()) {
			if(goalMap.containsKey(box.location)) {
				Goal goal = goalMap.get(box.location);
				if(goal.getAssignedBox().equals(box)) {
					goalCount++;
				}
			}
		}
		return goalCount;
	}

	public int g() {
		return g;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Boxes: ");
		for (Box b : getBoxes())
			sb.append("(").append(b.toString()).append("), ");
		sb.append("\nAgents: ");
		for (Agent a : getAgents())
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
				if (this.cellIsFree(newBoxLocation) && !fakeWalls.contains(newAgentLocation)) {
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
				if (box != null && box.getColor() == agent.getColor() && !fakeWalls.contains(boxLocation)) {
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

	private boolean cellIsFree(Location location) {
		return !walls[location.getRow()][location.getCol()] && 
				getObjectAt(location) == null;
	}

	public List<Action> extractActionPlan() {
		List<Action> plan = new ArrayList<>();
		State n = this;
		while (n.parent != null) {
			plan.add(n.action);
			n = n.parent;
		}
		Collections.reverse(plan);
		return plan;
	}

	public List<State> extractStatePlan() {
		List<State> plan = new ArrayList<>();
		State n = this;
		while (n.parent != null) {
			plan.add(n);
			n = n.parent;
		}
		Collections.reverse(plan);
		return plan;
	}

	public List<Location> extractLocationPlan(Agent agent) {
		List<Location> plan = new ArrayList<>();
		Location l = agent.getLocation();
		plan.add(l);
		
		Action last = null;
		List<Action> actions = extractActionPlan();
		for (Action action : actions) {
			if (action instanceof BoxAction) {
				BoxAction boxAction = (BoxAction) action;
				l = l.move(boxAction.getAgentDirection());
			}
			else if (action instanceof MoveAction) {
				MoveAction moveAction = (MoveAction) action;
				l = l.move(moveAction.getDirection());
			}
			last = action;
			plan.add(l);
		}
		if (last instanceof PushAction) {
			PushAction pushAction = (PushAction) action;
			l = l.move(pushAction.getBoxDirection());
			plan.add(l);
		}
		return plan;
	}

	public List<Location> getPath(Location loc, Location goal) {
		DistanceMap dm = DISTANCE_MAPS.get(goal);
		List<Location> path = new ArrayList<>();
		Location location = new Location(loc);
		int dist = dm.distance(location);
		while (dist > 1) {
			path.add(location);
			Location south = new Location(location.getRow()+1,location.getCol());
			Location north = new Location(location.getRow()-1,location.getCol());
			Location east = new Location(location.getRow(),location.getCol()+1);
			Location west = new Location(location.getRow(),location.getCol()-1);
			if(dm.distance(south) > 0 && dm.distance(south) < dist){
				dist = dist-1;
				location = south;
			}else if(dm.distance(north) > 0 && dm.distance(north) < dist){
				dist = dist-1;
				location = north;
			}else if(dm.distance(east) > 0 && dm.distance(east) < dist){
				dist = dist-1;
				location = east;
			}else if(dm.distance(west) > 0 && dm.distance(west) < dist){
				dist = dist-1;
				location = west;
			}else {
				
			}
		}
		path.add(location);
		return path;
	}

}
