import action.*;
import action.Action.Dir;
import state.*;
import task.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class Scheduler implements Runnable {
    private BufferedReader serverMessages;
    private State state;
    private Map<Integer, PriorityQueue<Task>> taskMap;
    private Map<Integer, Planner> plannerMap;
    private Map<Task, Integer> taskLockMap;
    private Map<Location,Integer> priorityMap;

    public Scheduler(State initialState, BufferedReader serverMessages) {
        this.serverMessages = serverMessages;
        // Get initial plan from initial state, queue them to priorityqueue
        state = initialState;

        plannerMap = new HashMap<>();
        taskMap = new HashMap<>();
        taskLockMap = new HashMap<>();

        Comparator<Task> taskComparator = (t1, t2) -> t2.getPriority() - t1.getPriority();

        for (Agent agent : state.getAgents()) {
        	plannerMap.put(agent.getId(), new Planner(agent.getId()));
        	taskMap.put(agent.getColor(), new PriorityQueue<>(taskComparator));
        }


        state.assignBoxesToGoals();
        priorityMap = new HashMap<>();
        calculateGoalPriorities();
        // Task of getting agent to box
		for (Box box : state.getAssignedBoxes()) {
			//TODO : Use the goal priorities
        	taskMap.get(box.getColor()).add(new MoveToBoxTask(box));
		}


        // Task of getting box to goal
        for (Goal goal : state.getGoals()) {
        	addTask(goal.getColor(), new GoalTask(priorityMap.get(goal.getLocation()),goal));
        }

        // Initial tasks
        // TODO - prioritize which agent takes which task, instead of random
        for (Agent agent : state.getAgents()) {
            assignTask(state, agent);
        }
    }
    
    private void calculateGoalPriorities() {
    	//NOT WORKING TOTALLY CORRECT
    	int size = state.getGoals().size();
    	priorityMap = new HashMap<>();
    	Map<Goal, Integer> currentPriorityMap = new HashMap<>();
    	Map <Goal, List<Goal>> goalPathMap = new HashMap<>();
    	List<Goal> goals = state.getGoals();
    	for(Goal goal : goals) {
    		goalPathMap.put(goal, new ArrayList<>());
    		Box box = goal.getAssignedBox();
    		
    		List<Location> shortestPath = state.getPath(box.getLocation(), goal);
    		for(Location l : shortestPath) {
    			if(State.goalMap.containsKey(l)) {
    				List<Goal> goalsCrossing = goalPathMap.get(goal);
    				goalsCrossing.add(State.goalMap.get(l));
    				goalPathMap.put(goal, goalsCrossing);
    			}
    		}
    	}
    	
    	//Goals which do not block other goals
    	int missingPriorities = size;
    	Set<Goal> hasPriority = new HashSet<>();
    	for(Goal goal : goals) {
    		currentPriorityMap.put(goal, 1);
    		List<Goal> goalsCrossing = goalPathMap.get(goal);
    		if(goalsCrossing.isEmpty()) {
    			currentPriorityMap.put(goal, 0);
    			missingPriorities--;
    			hasPriority.add(goal);
    		}
    	}
    	
    	while(missingPriorities > 0) {
    		for(Goal goal : goals) {
    			if(hasPriority.contains(goal)) {
    				continue;
    			}
        		
        		List<Goal> goalsCrossing = goalPathMap.get(goal);
        		int currentPriority = currentPriorityMap.get(goal);
        		int updatedPriority = currentPriority;
        		for(Goal gc : goalsCrossing) {
        			int tempPriority = currentPriorityMap.get(gc);
    				if(!goalPathMap.get(gc).contains(goal)) {
    					tempPriority++;
    				}
    				
    				if (tempPriority > updatedPriority) {
    					updatedPriority = tempPriority;
    				}
        		}
        		currentPriorityMap.put(goal, updatedPriority);
        		if(currentPriority == updatedPriority) {
    				currentPriorityMap.put(goal, currentPriority);
        			missingPriorities--;
        			hasPriority.add(goal);
        			continue;
    			}
        		if(updatedPriority >= size) {
        			currentPriorityMap.put(goal, size);
        			missingPriorities--;
        			hasPriority.add(goal);
        			continue;
        		}
        	}
    	}
    	
    	for(Goal goal : goals) {
    		priorityMap.put(goal.getLocation(), currentPriorityMap.get(goal));
    	}
    	
	}
    
    private void lockTask(Task task, int lock) {
    	Integer old = taskLockMap.get(task);
    	if (old != null) {
    		lock += old;
    	}
    	taskLockMap.put(task, lock);
    }
    
    private int unlockTask(Task task) {
    	Integer lock = taskLockMap.get(task);
    	if (lock != null) {
    		int val = lock - 1;
    		if (val <= 0) {
    			taskLockMap.remove(task);
    			taskMap.get(task.getAgent().getColor()).add(task);
    		}
    		else {
    			taskLockMap.put(task, val);
    		}
    		return val;
    	}
    	return -1;
    }
    
    private boolean allTasksCompleted() {
    	for (Integer color : taskMap.keySet()) {
    		Collection<Task> tasks = taskMap.get(color);
    		if (!tasks.isEmpty()) {
    			return false;
    		}
    	}
    	return true;
    }

    private Task assignTask(State state, Agent agent) {
    	PriorityQueue<Task> tasks = taskMap.get(agent.getColor());
    	Planner planner = plannerMap.get(agent.getId());
    		// TODO - I really think we should consider fixing this in another way. See notes.txt
    	Task task = null;
    	List<Task> denied = new LinkedList<>();
    	while (task == null && !tasks.isEmpty()) {
    		task = tasks.poll();
    		if (!task.assignAgent(agent)) {
    			denied.add(task);
    			task = null;
    		}
    	}
    	tasks.addAll(denied);
    	
    	if (task != null) {
    		if (!planner.addTask(state, task)) {
    			planner.clear();
    			int lock = 0;
    			if (task instanceof GoalTask) {
    				GoalTask goalTask = (GoalTask) task;
    				Task naiveTask = new NaiveGoalTask(2, goalTask.getGoal());
    				naiveTask.assignAgent(agent);
    				State terminalState = planner.createPlan(state, naiveTask);
    				List<Location> plan = terminalState.extractLocationPlan(agent);
    				for (Location location : plan) {
    					MovableObject object = state.getObjectAt(location);
    					if (object instanceof Agent) {
    						Agent moveAgent = (Agent) object;
    						if(moveAgent.getId() != agent.getId()) {
    							taskMap.get(moveAgent.getColor()).add(new MoveAgentTask(task.getPriority()+1, task, moveAgent, plan));
        						lock++;
    						}
    					}
    					else if (object instanceof Box) {
    						Box box = (Box) object;
    						if(box.getColor() != agent.getColor()) {
    							taskMap.get(box.getColor()).add(new MoveBoxTask(task.getPriority()+1, task, box, plan));
        						lock++;
    						}
    					}
    				}
    				lockTask(task, lock);
    			} else if (task instanceof ResolveTask) {
    				//TODO : This
    			} else if (task instanceof MoveToBoxTask) {
    				// TODO : This. This is similar to goaltask
				}
    			
    			if (!taskLockMap.containsKey(task)) {
    				tasks.add(task);
    			}
    			task = null;
    		}
    	}
    	return task;
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
    
    private void completeTasks(Planner planner) {
    	Queue<Task> tasks = planner.getTasks();
    	planner.clear();
    	while (!tasks.isEmpty()) {
			Task completedTask = tasks.poll();
			if (completedTask instanceof ResolveTask) {
				ResolveTask task = (ResolveTask) completedTask;
				Task resolved = task.getTaskToResolve();
				int priority = Math.max(resolved.getPriority(), task.getPriority() + 1);
				resolved.setPriority(priority);
				unlockTask(task.getTaskToResolve());
			}
		}
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
        long timeStart = System.currentTimeMillis();
		while (!solved) {
			boolean done = true;
			
			String cmd = "";
			for (Agent agent : state.getAgents()) {
				Planner planner = getPlanner(agent);
				if (planner.isEmpty()) {
					do {
						completeTasks(planner);
						assignTask(state, agent);
					} 
					while (planner.isEmpty() && !planner.getTasks().isEmpty());
				}
				if (!planner.isEmpty()) {
					done = false;
				}
				Action a = planner.poll();
				cmd += a.toString() + ";";
	        }
			solved = done && allTasksCompleted();
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
			for(Goal goal : state.getGoals()) {
				System.err.println("Goal " + goal.getLetter() + " has priority " + priorityMap.get(goal.getLocation()));
		    	
			}
			
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
					Location oldAgentLoc = agent.getLocation();
					state.applyAction(agent, action);
					//If box moved away from goal add goalTask again
					if(action instanceof BoxAction) {
						BoxAction boxAction = (BoxAction) action;
						Dir boxDir = boxAction.getBoxDirection();
						Location newAgentLoc = state.getAgent(agent).getLocation();
						Location newBoxLoc;
						newBoxLoc = (action instanceof PushAction) ? new Location(newAgentLoc.getRow() + Action.dirToRowChange(boxDir), newAgentLoc.getCol() + Action.dirToColChange(boxDir)) : new Location(oldAgentLoc);
						Location oldBoxLoc;
						oldBoxLoc = (action instanceof PushAction) ? new Location(newAgentLoc) : new Location(newBoxLoc.getRow() + Action.dirToRowChange(boxDir),newBoxLoc.getCol() + Action.dirToColChange(boxDir));
						if(State.goalMap.containsKey(oldBoxLoc)){
							Goal goal = State.goalMap.get(oldBoxLoc);
							if(goal.getLetter() == state.getBoxAt(newBoxLoc).getLetter()) {
								
								//TODO : Add move to box task first
								System.err.println("Re-adding goal task for goal " + goal.getLetter());
								addTask(goal.getColor(), new GoalTask(priorityMap.get(goal.getLocation()),goal));
								break;
							}
						}
					}
				}
	        }
			

			// TODO - handle conflicts better
			for (Location location : conflicts.keySet()) {
				Set<MovableObject> objects = conflicts.get(location);
				MovableObject priority = objects.stream().filter(x -> x instanceof Agent).findAny().get();
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
						Task task = priorityPlanner.getTasks().peek();
						Task avoidTask = new MoveBoxTask(task.getPriority() + 1, task, box, priorityPlanner.getPath(state));
						taskMap.get(box.getColor()).add(avoidTask);
						lockTask(task, 1);
						priorityPlanner.clear();
					}
				}
			}
		}
		double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
		System.err.println("Time spent on solving: " + timeSpent + " seconds.");
	}
}
