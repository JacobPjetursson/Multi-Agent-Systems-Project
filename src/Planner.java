import action.Action;
import action.NoOpAction;
import state.Agent;
import state.State;
import task.Task;

import java.util.*;

public class Planner {
	private Queue<Action> plan;
    private Agent agent;

    public Planner(Agent agent) {
        this.agent = agent;
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
    	plan.addAll(getPlan(state, task));
    }

    public ArrayList<Action> getPlan(State initialState, Task task) {
        HashSet<State> explored = new HashSet<>();
        PriorityQueue<State> frontier = new PriorityQueue<>(new StateComparator(task));
        frontier.add(initialState);
        explored.add(initialState);
        while (!frontier.isEmpty()) {
            State state = frontier.poll();
            if (task.isTerminal(state))
                return state.extractPlan();
            for (State child : state.getExpandedStates(agent)) {
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
    	
    	public StateComparator(Task task) {
			this.task = task;
		}
    	
		@Override
		public int compare(State s1, State s2) {
			return s1.f(task) - s2.f(task);
		}
    	
    }
}
