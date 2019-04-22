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

    Planner(int agentId) {
        this.agentId = agentId;
        plan = new LinkedList<>();
        tasks = new LinkedList<>();
    }

    Action poll() {
    	if (plan.isEmpty()) {
    		lastAction = new NoOpAction();
    	}
    	else {
    		lastAction = plan.poll();
    	}
    	return lastAction;
    }
    
    boolean isEmpty() {
    	return plan.isEmpty();
    }
    
    void undo() {
    	Queue<Action> plan = new LinkedList<>();
    	plan.add(this.lastAction);
    	plan.addAll(this.plan);
    	this.lastAction = new NoOpAction();
    	this.plan = plan;
    }
    
    Action getLastAction() {
    	return lastAction;
    }

    void clear() {
    	plan.clear();
    	tasks.clear();
    }
    
    public int getAgentId() {
    	return agentId;
    }

    boolean addTask(State state, Task task) {
    	State terminalState = createPlan(state, task);
        if (terminalState == null) {
            System.err.println("No plan was found");
            return false;
        }
        List<Action> actionList = terminalState.extractActionPlan();
        plan.addAll(actionList);
        tasks.add(task);
        return true;
    }
    
    Queue<Task> getTasks() {
    	return new LinkedList<>(tasks);
    }

    public int getSize() {
        return plan.size();
    }

    Queue<Action> getPlan() {
        return plan;
    }

    State createPlan(State initialState, Task task) {
    	initialState = initialState.clone();
    	task.initializeState(initialState);
        Set<State> explored = new HashSet<>();
        PriorityQueue<State> frontier = new PriorityQueue<>(new StateComparator(task));
        frontier.add(initialState);
        explored.add(initialState);
        while (!frontier.isEmpty()) {
            State state = frontier.poll();
            if (task.isTerminal(state)) {
                return state;
            }
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
