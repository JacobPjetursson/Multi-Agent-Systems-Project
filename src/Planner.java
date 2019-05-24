import action.Action;
import action.BoxAction;
import action.MoveAction;
import action.NoOpAction;
import state.Location;
import state.State;
import task.AgentToGoalTask;
import task.GoalTask;
import task.MoveToBoxTask;
import task.SimpleTask;
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
    
    public boolean isEmpty() {
    	return plan.isEmpty();
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
    
    public void addDelay() {
    	Queue<Action> plan = new LinkedList<>();
    	plan.add(new NoOpAction());
    	plan.addAll(this.plan);
    	this.plan = plan;
    }
    
    public int getAgentId() {
    	return agentId;
    }

    boolean addTask(State state, Task task) {

        if (!createSimplePlan(state, task)) {
            return false;
        }
    	State terminalState = createPlan(state, task);
        if (terminalState == null) {
            return false;
        }
        List<Action> actionList = terminalState.extractActionPlan();
        plan.addAll(actionList);
        tasks.add(task);
        return true;
    }
    
    public Queue<Task> getTasks() {
    	return new LinkedList<>(tasks);
    }
    
    public Task getCurrentTask() {
    	return tasks.peek();
    }
    
    public Task getNextTask() {
    	if (tasks.isEmpty()) return null;
    	return tasks.peek().getNextTask();
	}

    public int getSize() {
        return plan.size();
    }

    Queue<Action> getPlan() {
        return plan;
    }
    
    public List<Location> getPath(State state) {
    	List<Location> path = new LinkedList<>();
    	Location location = state.getAgent(getAgentId()).getLocation();
    	for (Action action : getPlan()) {
    		if (action instanceof MoveAction) {
    			MoveAction moveAction = (MoveAction) action;
    			location = location.move(moveAction.getDirection());
    		}
    		else if (action instanceof BoxAction) {
    			BoxAction boxAction = (BoxAction) action;
    			location = location.move(boxAction.getAgentDirection());
    		}
    		path.add(location);
    	}
    	return path;
    }
    
    private boolean isSolvable(State state, Task task) {
    	if(task instanceof GoalTask) {
    		task = (GoalTask) task;
    		if((state.getBoxAt(task.getGoalLocation()) != null 
    			&& state.getBoxAt(task.getGoalLocation()).getColor() != state.getAgent(agentId).getColor())
    			|| (state.getAgentAt(task.getGoalLocation()) != null	&& !state.getAgentAt(task.getGoalLocation()).equals(state.getAgent(agentId)))){
    			return false;
    		}
    		List<Location> shortestPath = state.getPath(state.getBox(((GoalTask) task).getBox()).getLocation(), task.getGoalLocation());
    		for(Location loc : shortestPath) {
    			if(State.hallways.contains(loc)) {
    				if((state.getBoxAt(loc) != null 
    		    		&& state.getBoxAt(loc).getColor() != state.getAgent(agentId).getColor())
    		    		|| (state.getAgentAt(loc) != null && !state.getAgentAt(loc).equals(state.getAgent(agentId)))){
    		    		return false;
    		    	}
    			}
    		}
    	}
    	if(task instanceof AgentToGoalTask) {
    		task = (AgentToGoalTask) task;
    		if((state.getBoxAt(task.getGoalLocation()) != null 
    			&& state.getBoxAt(task.getGoalLocation()).getColor() != state.getAgent(agentId).getColor())
    				|| (state.getAgentAt(task.getGoalLocation()) != null	&& !state.getAgentAt(task.getGoalLocation()).equals(state.getAgent(agentId)))){
    			return false;
    		}
    		List<Location> shortestPath = state.getPath(state.getAgent(task.getAgent()).getLocation(), task.getGoalLocation());
    		for(Location loc : shortestPath) {
    			if(State.hallways.contains(loc)) {
    				if((state.getBoxAt(loc) != null 
        		    	&& state.getBoxAt(loc).getColor() != state.getAgent(agentId).getColor())
        		    	|| (state.getAgentAt(loc) != null && !state.getAgentAt(loc).equals(state.getAgent(agentId)))){
        		   		return false;
        		   	}
    			}
    		}
    	}
    	if (task instanceof MoveToBoxTask) {
    		task = (MoveToBoxTask) task;
    		List<Location> shortestPath = state.getPath(state.getAgent(task.getAgent()).getLocation(), state.getBox(((MoveToBoxTask) task).getBox()).getLocation());
    		for(Location loc : shortestPath) {
    			if(State.hallways.contains(loc)) {
    				if((state.getBoxAt(loc) != null 
            		    && state.getBoxAt(loc).getColor() != state.getAgent(agentId).getColor())
            		   	|| (state.getAgentAt(loc) != null && !state.getAgentAt(loc).equals(state.getAgent(agentId)))){
            			return false;
            	   	}
    			}
    		}
    	}
    	
		return true;
    }
    
    

    State createPlan(State initialState, Task task) {
    	initialState = initialState.clone();
    	task.initializeState(initialState);
    	if(!isSolvable(initialState,task)) {
    		return null;
    	}
    	int max = task.estimatedTime(initialState) * 2;
        Set<State> explored = new HashSet<>();
        PriorityQueue<State> frontier = new PriorityQueue<>(new StateComparator(task));
        frontier.add(initialState);
        explored.add(initialState);
        while (!frontier.isEmpty()) {
            State state = frontier.poll();
            if (task.isTerminal(state)) {
                return state;
            }
            if((state.g() > max || state.f(task)-state.g() > max * 2) && !(task.getNaive() == null) ) {
            	
            	break;
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

    private boolean createSimplePlan(State initialState, Task task) {
        Location loc = task.getGoalLocation();
        if (loc == null) // Only create simple plan for tasks that has a specific goal (MoveAgentToBox, BoxToGoal, etc.)
            return true;

        SimpleTask t = new SimpleTask(task.getGoalLocation(), task.getPriority());
        t.assignAgent(task.getAgent());
        return createPlan(initialState, t) != null;
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