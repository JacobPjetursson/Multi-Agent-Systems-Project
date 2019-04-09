package task;

public abstract class ResolveTask extends Task {
	
	private Task taskToResolve;

	public ResolveTask(int priority, Task taskToResolve) {
		super(priority);
		this.taskToResolve = taskToResolve;
	}
	
	public Task getTaskToResolve() {
		return taskToResolve;
	}
	
}
