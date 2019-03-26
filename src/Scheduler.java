import state.State;
import task.Task;

import java.io.BufferedReader;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Queue;

public class Scheduler {
    private Queue<Task> queue;
    private BufferedReader serverMessages;

    public Scheduler(State initialState, BufferedReader serverMessages) {
        this.serverMessages = serverMessages;
        // Get initial plan from initial state, queue them to priorityqueue
        queue = new PriorityQueue<>(new TaskComparator());
    }


    private class TaskComparator implements Comparator<Task> {
        @Override
        public int compare(Task t1, Task t2) {
            if (t1.getPriority() > t2.getPriority())
                return 1;
            else if (t2.getPriority() > t1.getPriority())
                return -1;
            return 0;
        }
    }
}
