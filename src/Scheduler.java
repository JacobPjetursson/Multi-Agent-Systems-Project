import state.Agent;
import state.Box;
import state.Goal;
import state.Location;
import state.MovableObject;
import state.State;
import task.AvoidConflictTask;
import task.GoalTask;
import task.Task;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

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
        	addTask(goal.getColor(), new GoalTask(goal));
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
    
    private void addTask(Integer color, Task task) {
    	taskMap.get(color).add(task);
    }
    
    private void addTasks(Integer color, Collection<Task> tasks) {
    	taskMap.get(color).addAll(tasks);
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
    	Location location = agent.getLocation();
    	if (action instanceof MoveAction) {
			MoveAction moveAction = (MoveAction) action;
			location = location.move(moveAction.getDirection());
		}
		if (action instanceof PushAction) {
			PushAction pushAction = (PushAction) action;
			location = location.move(pushAction.getAgentDirection()).move(pushAction.getBoxDirection());
		}
		else if (action instanceof PullAction) {
			PullAction pullAction = (PullAction) action;
			location = location.move(pullAction.getAgentDirection());
		}
		addConflict(conflictMap, agent, location);
		MovableObject object = state.getObjectAt(location);
		if (object != null) {
			addConflict(conflictMap, object, location);
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
				MovableObject priority = objects.stream().findAny().get();
				if (priority instanceof Agent) {
					Agent priorityAgent = (Agent) priority;
					Planner priorityPlanner = getPlanner(priorityAgent);
					priorityPlanner.undo();
					
					Set<MovableObject> rest = objects.stream().filter(x -> !x.equals(priority)).collect(Collectors.toSet());
					for (MovableObject object : rest) {
						if (object instanceof Agent) {
							Agent agent = (Agent) object;
							Planner planner = getPlanner(agent);
							addTasks(agent.getColor(), planner.getTasks());
							planner.clear();
							planner.addTask(state, new AvoidConflictTask(1, priorityAgent.getId(), priorityPlanner.getPlan()));
						}
						else if (object instanceof Box) {
							Box box = (Box) object;
							// Well fuck
						}
					}
				}
			}
		}
	}
}
