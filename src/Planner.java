import action.Action;
import action.NoOpAction;
import state.Agent;
import state.State;
import task.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

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

    public ArrayList<Action> getPlan(State state, Task task) {
        HashSet<State> explored;
        PriorityQueue<State> frontier = new PriorityQueue<>(new StateComparator());
        frontier.add(state);
        while (!frontier.isEmpty()) {
            for (State child : state.getChildren(agent)) {
                // TODO
            }
        }
        return null;
    }

    public class StateComparator implements Comparator<State> {

        @Override
        public int compare(State s1, State s2) {
            int g1 = s1.g();
            int g2 = s2.g();
            if (g1 > g2)
                return 1;
            else if (g1 < g2)
                return 2;
            return 0;
        }
    }
}
