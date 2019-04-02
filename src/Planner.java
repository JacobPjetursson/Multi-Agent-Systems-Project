import action.Action;
import action.NoOpAction;
import state.State;
import task.Task;

import java.util.*;

public class Planner {
	private Queue<Action> plan;
	private Action lastAction;
    private int agentId;

    public Planner(int agentId) {
        this.agentId = agentId;
        plan = new LinkedList<>();
    }

    public Action poll() {
    	if (plan.isEmpty()) {
    		lastAction = new NoOpAction();
    	}
    	else {
    		lastAction = plan.poll();
    	}
    	return lastAction;
    }
    
    public Action getLastAction() {
    	return lastAction;
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

    public Queue<Action> getPlan() {
        return plan;
    }

    private ArrayList<Action> createPlan(State initialState, Task task) {
        HashSet<State> explored = new HashSet<>();
        PriorityQueue<State> frontier = new PriorityQueue<>(new StateComparator(task));
        frontier.add(initialState);
        explored.add(initialState);
        while (!frontier.isEmpty()) {
            State state = frontier.poll();
            if (task.isDone(state))
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


    private class StateComparator implements Comparator<State> {
    	Task task;
    	
    	StateComparator(Task task) {
			this.task = task;
		}
    	
		@Override
		public int compare(State s1, State s2) {
			return s1.f(task) - s2.f(task);
		}
    	
    }
}
