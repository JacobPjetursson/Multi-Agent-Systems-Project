import state.Agent;
import state.State;
import task.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import action.Action;
import action.NoOpAction;

public class Scheduler implements Runnable {
    private Queue<Task> queue;
    private BufferedReader serverMessages;
    private State state;
    private Map<Integer, Queue<Action>> actionMap;

    public Scheduler(State initialState, BufferedReader serverMessages) {
        this.serverMessages = serverMessages;
        // Get initial plan from initial state, queue them to priorityqueue
        state = initialState;
        queue = new PriorityQueue<>(new TaskComparator());
        actionMap = new HashMap<>();
        for (Agent agent : state.getAgents()) {
        	actionMap.put(agent.getId(), new LinkedList<>());
        }
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

	@Override
	public void run() {
		boolean solved = false;
		while (!solved) {
			String cmd = "";
			for (Agent agent : state.getAgents()) {
	        	Queue<Action> q = actionMap.get(agent.getId());
	        	Action a = new NoOpAction();
	        	if (!q.isEmpty()) {
	        		a = q.poll();
	        	}
	        	cmd += a.toString() + ";";
	        }
			cmd = cmd.substring(0, cmd.length()-1);
			System.out.println(cmd);
			System.err.println(cmd);
			
			String message = "";
			try {
				message = serverMessages.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			System.err.println(message);
			String[] feedback = message.split(";");
			for (Agent agent : state.getAgents()) {
				boolean error = !Boolean.parseBoolean(feedback[agent.getId()]);
				if (error) {
					System.err.println("Make new plan for Agent"+agent.getId());
					actionMap.get(agent.getId()).clear();
					// TODO: Planner make new plan!!
				}
	        }
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
