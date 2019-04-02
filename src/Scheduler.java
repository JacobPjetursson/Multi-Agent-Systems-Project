import state.Agent;
import state.Box;
import state.Goal;
import state.Location;
import state.MovableObject;
import state.State;
import task.GoalTask;
import task.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import action.*;

public class Scheduler implements Runnable {
    private BufferedReader serverMessages;
    private State state;
    private Map<Integer, PriorityQueue<Task>> taskMap;
    private Map<Integer, Planner> plannerMap;

    public Scheduler(State initialState, BufferedReader serverMessages) {
        this.serverMessages = serverMessages;
        // Get initial plan from initial state, queue them to priorityqueue
        state = initialState;
        
        plannerMap = new HashMap<>();
        taskMap = new HashMap<>();

        for (Agent agent : state.getAgents()) {
        	plannerMap.put(agent.getId(), new Planner(agent.getId()));
        	taskMap.put(agent.getColor(), new PriorityQueue<>());
        }
        for (Goal goal : state.getGoals()) {
        	taskMap.get(goal.getColor()).add(new GoalTask(goal));
        }

        // Initial tasks
        for (Agent agent : state.getAgents())
            getTask(state, agent);


    }
    
    private void getTask(State state, Agent agent) {
    	PriorityQueue<Task> tasks = taskMap.get(agent.getColor());
    	Planner planner = plannerMap.get(agent.getId());
    	if (!tasks.isEmpty()) {
    		Task task = tasks.poll();
    		task.assignAgent(agent);
    		planner.addTask(state, task);
    	}
    }
    
    private Planner getPlanner(Agent agent) {
    	return plannerMap.get(agent.getId());
    }
    
    private void addConflict(Map<Location, Set<MovableObject>> conflictMap, MovableObject object, Location location) {
    	Set<MovableObject> conflictList = conflictMap.get(location);
		if (conflictList == null) {
			conflictList = new HashSet<>();
			conflictMap.put(location, conflictList);
		}
		conflictList.add(object);
    }
    
    private void collectConflicts(Map<Location, Set<MovableObject>> conflictMap, Agent agent, Action action) {
    	if (action instanceof MoveAction) {
			MoveAction moveAction = (MoveAction) action;
			Location location = agent.getLocation().move(moveAction.getDirection());
			addConflict(conflictMap, agent, location);
			MovableObject object = state.getObjectAt(location);
			if (object != null) {
				addConflict(conflictMap, object, location);
			}
		}
		if (action instanceof PushAction) {
			PushAction pushAction = (PushAction) action;
			Location location = agent.getLocation().move(pushAction.getAgentDirection());
			Box box = state.getBoxAt(location);
			location = location.move(pushAction.getBoxDirection());
			addConflict(conflictMap, box, location);
			MovableObject object = state.getObjectAt(location);
			if (object != null) {
				addConflict(conflictMap, object, location);
			}
		}
		else if (action instanceof PullAction) {
			PullAction pullAction = (PullAction) action;
			Location location = agent.getLocation();
			Box box = state.getBoxAt(location.move(pullAction.getBoxDirection()));
			addConflict(conflictMap, box, location);
			MovableObject object = state.getObjectAt(location);
			if (object != null) {
				addConflict(conflictMap, object, location);
			}
		}
    }

	@Override
	public void run() {
		boolean solved = false;
		while (!solved) {
			try { // TODO - Remove at release build
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			boolean done = true;
			String cmd = "";
			for (Agent agent : state.getAgents()) {
				Planner planner = getPlanner(agent);
				Action a = planner.poll();
				if (a.toString().equals(NoOpAction.COMMAND)) {
					getTask(state, agent);
					a = planner.poll();
				}
				else {
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
			
			System.err.println("RESPONSE: " + message);
			String[] feedback = message.split(";");
			Map<Location, Set<MovableObject>> conflicts = new HashMap<>();
			for (Agent agent : state.getAgents()) {
				boolean error = !Boolean.parseBoolean(feedback[agent.getId()]);
				Planner planner = getPlanner(agent);
				Action action = planner.getLastAction();
				if (error) {
					System.err.println("Conflict involving Agent"+agent.getId());
					collectConflicts(conflicts, agent, action);
				}
				else {
					state.applyAction(agent, action);
				}
	        }
			
			// TODO - handle conflicts
			for (Location location : conflicts.keySet()) {
				Set<MovableObject> objects = conflicts.get(location);
				for (MovableObject object : objects) {
					if (object instanceof Agent) {
						Agent agent = (Agent) object;
						// Do something
					}
					else if (object instanceof Box) {
						Box box = (Box) object;
						
					}
				}
			}
		}
	}
}
