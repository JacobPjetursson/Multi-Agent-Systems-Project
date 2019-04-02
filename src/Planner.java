import action.Action;
import action.NoOpAction;
import state.State;
import task.Task;

import java.util.*;

public class Planner {
	private Queue<Action> plan;
	private Queue<Task> tasks;
	private Action lastAction;
    private int agentId;

    public Planner(int agentId) {
        this.agentId = agentId;
        plan = new LinkedList<>();
        tasks = new LinkedList<>();
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
    
    public void undo() {
    	Queue<Action> plan = new LinkedList<>();
    	plan.add(this.lastAction);
    	plan.addAll(this.plan);
    	this.lastAction = new NoOpAction();
    	this.plan = plan;
    }
    
    public Action getLastAction() {
    	return lastAction;
    }

    public void clear() {
    	plan.clear();
    	tasks.clear();
    }
    
    public int getAgentId() {
    	return agentId;
    }

    public boolean addTask(State state, Task task) {
        List<Action> actionList = createPlan(state, task);
        if (actionList == null) {
            System.err.println("No plan was found");
            return false;
        }
        plan.addAll(actionList);
        tasks.add(task);
        return true;
    }
    
    public Queue<Task> getTasks() {
    	return new LinkedList<>(tasks);
    }

    public int getSize() {
        return plan.size();
    }

    public Queue<Action> getPlan() {
        return plan;
    }

    private List<Action> createPlan(State initialState, Task task) {
        Set<State> explored = new HashSet<>();
        PriorityQueue<State> frontier = new PriorityQueue<>(new StateComparator(task));
        frontier.add(initialState);
        explored.add(initialState);
        while (!frontier.isEmpty()) {
            State state = frontier.poll();
            if (task.isTerminal(state))
                return state.extractPlan();
            for (State child : state.getExpandedStates(agentId)) {
            	if (task.updateState(child) && !explored.contains(child)) {
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
