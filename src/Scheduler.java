import state.Agent;
import state.Goal;
import state.State;
import task.GoalTask;
import task.PlanTask;
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
    private Map<Integer, PriorityQueue<Task>> taskMap;
    private Map<Integer, Planner> plannerMap;

    public Scheduler(State initialState, BufferedReader serverMessages) {
        this.serverMessages = serverMessages;
        // Get initial plan from initial state, queue them to priorityqueue
        state = initialState;
        queue = new PriorityQueue<>(new TaskComparator());
        
        plannerMap = new HashMap<>();
        taskMap = new HashMap<>();
        for (Agent agent : state.getAgents()) {
        	plannerMap.put(agent.getId(), new Planner(agent));
        	taskMap.put(agent.getColor(), new PriorityQueue<>());
        }
        for (Goal goal : state.getGoals()) {
        	taskMap.get(goal.getColor()).add(new GoalTask(goal));
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
    
    private void getTask(State state, Agent agent) {
    	PriorityQueue<Task> tasks = taskMap.get(agent.getColor());
    	Planner planner = plannerMap.get(agent.getId());
    	if (!tasks.isEmpty()) {
    		Task task = tasks.poll();
    		planner.addTask(state, task);
    	}
    }

	@Override
	public void run() {
		boolean solved = false;
		while (!solved) {
			boolean done = true;
			String cmd = "";
			for (Agent agent : state.getAgents()) {
				Planner planner = plannerMap.get(agent.getId());
				Action a = planner.poll();
				if (a.toString().equals(NoOpAction.COMMAND)) {
					getTask(state, agent);
					a = planner.poll();
				}
				if (!a.toString().equals(NoOpAction.COMMAND)) {
					done = false;
				}
	        	cmd += a.toString() + ";";
	        }
			solved = done;
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
					plannerMap.get(agent.getId()).clear();
					// TODO: Planner make new plan!!
				}
				else {
					// TODO: Update global state
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
