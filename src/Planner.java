import action.Action;
import state.Agent;
import state.State;
import task.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Planner {
    Agent agent;

    public Planner(Agent agent) {
        this.agent = agent;
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
