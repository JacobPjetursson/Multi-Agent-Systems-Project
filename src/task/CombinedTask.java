package task;

import state.Location;
import state.State;

import java.util.ArrayList;
import java.util.List;

public class CombinedTask extends Task {
    List<Task> tasks;

    public CombinedTask(ArrayList<Task> tasks) {
        super(10);
        this.tasks = tasks;
    }

    public CombinedTask(int priority, ArrayList<Task> tasks) {
        super(priority);
        this.tasks = tasks;
    }

    @Override
    public boolean isTerminal(State state) {
        for (Task t : tasks) {
            if (!t.isTerminal(state))
                return false;
        }
        return true;
    }

    @Override
    public int h(State state) {
        int combinedH = 0;
        for (Task t : tasks)
            combinedH += t.h(state);
        return combinedH;
    }

    @Override
    public boolean updateState(State state) {
        return false;
    }

    @Override
    public void initializeState(State state) {

    }

    @Override
    public Location getGoalLocation() {
        return null;
    }

    @Override
    public Task getNaive() {
        return null;
    }

    @Override
    public Task getNextTask() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof CombinedTask) {
            CombinedTask task = (CombinedTask) o;
            return task.tasks.equals(this.tasks);
        }
        return false;
    }

	@Override
	public int estimatedTime(State state) {
		int combinedTime = 0;
        for (Task t : tasks)
            combinedTime += t.estimatedTime(state);
        return combinedTime;
	}
}
