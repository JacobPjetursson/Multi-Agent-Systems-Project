import action.Action;
import action.NoOpAction;
import state.State;
import task.Task;

import java.util.*;

public class Planner {
	private Queue<Action> plan;
    private int agentId;

    public Planner(int agentId) {
        this.agentId = agentId;
        plan = new LinkedList<>();
    }

    public Action poll() {
    	if (plan.isEmpty()) {
    		return new NoOpAction();
    	}
    	return plan.poll();
    }

    public void clear() {
    	plan.clear();
    }

    public void addTask(State state, Task task) {
        ArrayList<Action> actionList = createPlan(state, task);
        if (actionList == null)
            System.err.println("No plan was found");
        else
    	    plan.addAll(actionList);
    }

    public int getSize() {
        return plan.size();
    }

    public ArrayList<Action> createPlan(State initialState, Task task) {
        HashSet<State> explored = new HashSet<>();
        PriorityQueue<State> frontier = new PriorityQueue<>(new StateComparator(task));
        frontier.add(initialState);
        explored.add(initialState);
        while (!frontier.isEmpty()) {
            State state = frontier.poll();
            if (task.isTerminal(state))
                return state.extractPlan();
            for (State child : state.getExpandedStates(agentId)) {
                if (!explored.contains(child)) {
                    frontier.add(child);
                    explored.add(child);
                }
            }
        }
        return null;
    }

    public Queue<Action> getPlan() {
        return plan;
    }
    
    private class StateComparator implements Comparator<State> {
    	
    	Task task;
    	
    	public StateComparator(Task task) {
			this.task = task;
		}
    	
		@Override
		public int compare(State s1, State s2) {
			return s1.f(task) - s2.f(task);
		}
    	
    }
}
