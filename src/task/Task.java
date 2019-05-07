package task;

import state.Agent;
import state.Location;
import state.State;

public abstract class Task implements Comparable<Task> {
    private int priority;
    private Agent agent;
    
    public Task(int priority) {
		this.priority = priority;
	}

    public int getPriority() {
        return priority;
    }
    
    public void setPriority(int priority) {
		this.priority = priority;
	}
    
    @Override
    public int compareTo(Task o) {
    	return this.priority - o.priority;
    }
    
    public boolean assignAgent(Agent agent) {
    	this.agent = agent;
    	return true;
    }
    
    public Agent getAgent() {
    	return agent;
    }

	public abstract boolean isTerminal(State state);
	public abstract int h(State state);
	public abstract boolean updateState(State state);
	public abstract void initializeState(State state);
	public abstract Location getGoalLocation(); // Used for simple task
	public abstract Task getNaive();
	public abstract Task getNextTask();
}
