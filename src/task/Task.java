package task;

import state.Agent;
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
    
    @Override
    public int compareTo(Task o) {
    	return this.priority - o.priority;
    }
    
    public void assignAgent(Agent agent) {
    	this.agent = agent;
    }
    
    public Agent getAgent() {
    	return agent;
    }

	public abstract boolean isDone(State state);
	public abstract int h(State state);
}
