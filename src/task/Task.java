package task;

import state.State;

public abstract class Task implements Comparable<Task> {
    int priority;
    
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

	public abstract boolean isTerminal(State state);
}
