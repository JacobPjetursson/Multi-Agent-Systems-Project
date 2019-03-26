package state;

import action.Action;
import action.MoveAction;
import task.Task;

import action.PullAction;
import action.PushAction;
import java.util.*;

public class State{
	public static final Map<Location, DistanceMap> DISTANCE_MAPS = new HashMap<>();
	public static int ROWS;
	public static int COLS;
    private static final Random RNG = new Random(1);


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

    public ArrayList<State> getExpandedStates(Agent agent) {
        ArrayList<State> expandedStates = new ArrayList<>(Action.EVERY.length);
        for (Action action : Action.EVERY) {
            // Determine applicability of action
            int newAgentRow = agent.getLocation().getRow() + Action.dirToRowChange(action.dir1);
            int newAgentCol = agent.getLocation().getCol() + Action.dirToColChange(action.dir1);

            if (action instanceof MoveAction) {
                // Check if there's a wall or box on the cell to which the agent is moving
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    State child = new State(this, this.action);
                    for (Agent a : child.getAgents()) { // Only expand for input agent
                        if (a.equals(agent)) {
                            a.getLocation().setRow(newAgentRow);
                            a.getLocation().setRow(newAgentCol);
                        }
                    }
                    expandedStates.add(child);
                }
            } else if (action instanceof PushAction) {
                // Make sure that there's actually a box to move
                if (this.boxAt(newAgentRow, newAgentCol)) {
                    int newBoxRow = newAgentRow + Action.dirToRowChange(action.dir2);
                    int newBoxCol = newAgentCol + Action.dirToColChange(action.dir2);
                    // .. and that new cell of box is free
                    if (this.cellIsFree(newBoxRow, newBoxCol)) {
                        State child = new State(this, this.action);
                        for (Agent a : child.getAgents()) { // Only expand for input agent
                            if (a.equals(agent)) {
                                a.getLocation().setRow(newAgentRow);
                                a.getLocation().setRow(newAgentCol);
                            }
                        }
                        for (Box b : child.getBoxes()) {
                            if (b.location.getRow() == newAgentRow && b.location.getCol() == newAgentCol) {
                                b.location.setRow(newBoxRow);
                                b.location.setCol(newBoxCol);
                            }

                        }
                        expandedStates.add(child);
                    }
                }
            } else if (action instanceof PullAction) {
                // Cell is free where agent is going
                if (this.cellIsFree(newAgentRow, newAgentCol)) {
                    int boxRow = agent.getLocation().getRow() + Action.dirToRowChange(action.dir2);
                    int boxCol = agent.getLocation().getCol() + Action.dirToColChange(action.dir2);
                    // .. and there's a box in "dir2" of the agent
                    if (this.boxAt(boxRow, boxCol)) {
                        State child = new State(this, this.action);
                        for (Agent a : child.getAgents()) { // Only expand for input agent
                            if (a.equals(agent)) {
                                a.getLocation().setRow(newAgentRow);
                                a.getLocation().setRow(newAgentCol);
                            }
                        }
                        for (Box b : child.getBoxes()) {
                            if (b.location.getRow() == boxRow && b.location.getCol() == boxCol) {
                                b.location.setRow(agent.getLocation().getRow());
                                b.location.setCol(agent.getLocation().getCol());
                            }

                        }
                        expandedStates.add(child);
                    }
                }
            }
        }
        Collections.shuffle(expandedStates, RNG);
        return expandedStates;
    }

    private boolean cellIsFree(int row, int col) {
	    boolean boxFree = true;
	    for (Box b : boxes) {
	        Location boxLoc = b.getLocation();
	        if (boxLoc.getRow() == row && boxLoc.getCol() == col) // TODO optimize
	            boxFree = false;
        }
        return !walls[row][col] && boxFree;
    }

    private boolean boxAt(int row, int col) {
        for (Box b : boxes) {
            Location boxLoc = b.getLocation();
            if (boxLoc.getRow() == row && boxLoc.getCol() == col)
                return false;
        }
        return true;
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

    private int h() {
	    return 0;
    }
}
