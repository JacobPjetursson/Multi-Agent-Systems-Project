import action.*;

import action.Action.Dir;
import state.*;
import task.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class Scheduler implements Runnable {
	// To get nice solutions if multiple rooms exists split up everything into rooms. Dont think we have the time though
	private BufferedReader serverMessages;
	private State state;
	private Map<Integer, PriorityQueue<Task>> taskMap;
	private Map<Integer, Planner> plannerMap;
	private Map<Task, Integer> taskLockMap;

	private static Map<Location,Integer> priorityMap;
	private int taskPriority = 0;

	Scheduler(State initialState, BufferedReader serverMessages) {
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

		state.assignObjectsToGoals();
		priorityMap = new HashMap<>();
		calculateGoalPriorities();
		State.freeBoxes = state.getBoxes().size();
		State.freeGoals = state.getGoals().size();
		calculateSafeLocations(state);
		findHallways();

		// Task of getting box to goal
		for (Goal goal : state.getGoals())
			addGoalTask(goal);
		
		// Initial tasks
		// TODO - prioritize which agent takes which task, instead of random
		for (Agent agent : state.getAgents()) {
			assignTask(state, agent);
		}
	}

	private void findHallways() {
		Set<Location> hallways = new HashSet<>();
		for(int row = 1; row < State.ROWS-1; row++) {
			for(int col = 1 ; col < State.COLS-1; col++) {
				if(State.walls[row-1][col] && State.walls[row+1][col]) {
					hallways.add(new Location(row,col));
					//hallways.add(new Location(row,col+1));
					//hallways.add(new Location(row,col-1));
				}else if(State.walls[row][col-1] && State.walls[row][col+1]) {
					hallways.add(new Location(row,col));
					//hallways.add(new Location(row+1,col));
					//hallways.add(new Location(row-1,col));
				}
			}
		}
		State.hallways = hallways;
		
	}

	private void calculateSafeLocations(State state) {
		List<Goal> goals = new ArrayList<>(state.getGoals());
		Map<Location,Integer> paths = new HashMap<>();
		for(Goal goal : goals) {
			if(state.getBoxAt(goal.getLocation()) != null || priorityMap.get(goal.getLocation()) <= taskPriority-1) {
				//if(state.getBoxAt(goal.getLocation()) != null)
					continue;
			}
			MovableObject object = goal.getAssignedObj();
			Location location;
			if(goal instanceof AgentGoal) {
				location = state.getAgent((Agent) object).getLocation();
			}else {
				Box b = state.getBox((Box) object);
				if (b == null) // box converted to wall
					location = null;
				else
					location = state.getBox((Box) object).getLocation();
			}


			List<Location> shortestPath;
			if(location == null) {
				shortestPath = new ArrayList<>();
			}else {
				shortestPath = state.getPath(location, goal.getLocation());
			}
			for(Location l : shortestPath) {
				if(paths.containsKey(l)) {
					paths.put(l,paths.get(l));
				}else {
					paths.put(l,1);
				}
			}
		}
		
		
		State.safeLocation = new HashMap<>();
		for(int row = 0; row < State.ROWS; row++) {
			for(int col = 0; col < State.COLS; col++) {
				Location location = new Location(row,col);
				if(!State.walls[row][col]) {
					int safeValue = Integer.MAX_VALUE;
					DistanceMap dm = State.DISTANCE_MAPS.get(location);
					for(Location loc : paths.keySet()) {
						int temp = dm.distance(loc);
						if(temp < safeValue) {
							safeValue = temp;
						}
					}
					if(paths.containsKey(location) && safeValue <= 0) {
						safeValue = paths.get(location);
					}
					
					State.safeLocation.put(location, Math.min((int) (safeValue*(taskPriority)),(taskPriority)*taskPriority));
					
					//State.safeLocation.put(location, Math.min((int) (safeValue*(State.freeBoxes)),State.freeBoxes*State.freeBoxes));
				}else {
					State.safeLocation.put(location, -1);
				}
			}
		}
		
	}

	private void addGoalTask(Goal goal) {
		int priority = priorityMap.get(goal.getLocation());
		Task task = null;
		if (goal instanceof BoxGoal) {
			Box box = (Box) goal.getAssignedObj();
			GoalTask goalTask = new GoalTask(priority, box, goal);
			task = new MoveToBoxTask(priority, box, goalTask);
		} else {
			for (Agent agent : state.getAgents()) {
				if (goal.getLetter() == agent.getLetter()) {
					task = new AgentToGoalTask(priority, goal, agent);
					break;
				}
			}
		}
		addTask(goal.getColor(), task);
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

	private void calculateGoalPriorities() {
		int size = state.getGoals().size();
		priorityMap = new HashMap<>();
		Map<Goal, Integer> currentPriorityMap = new HashMap<>();
		Map <Goal, List<Goal>> goalPathMap = new HashMap<>();
		List<Goal> goals = new ArrayList<>(state.getGoals());
		for(Goal goal : goals) {
			goalPathMap.put(goal, new ArrayList<>());
			Location location = goal.getAssignedObj().getLocation();
			List<Location> shortestPath = state.getPath(location, goal.getLocation());
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
			currentPriorityMap.put(goal, 2);
			List<Goal> goalsCrossing = goalPathMap.get(goal);
			if(goalsCrossing.isEmpty()) {
				currentPriorityMap.put(goal, 1);
				missingPriorities--;
				hasPriority.add(goal);
			}
			
		}
		
		
		
		while(missingPriorities > 0) {
			for(Goal goal : goals) {
				if(hasPriority.contains(goal))
					continue;

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
				}
			}
		}
		missingPriorities = 0;
		hasPriority = new HashSet<>();
		Map<Integer, Integer> agentPrioMap = new HashMap<>();
		for(Goal goal : goals) {
			if(goal instanceof BoxGoal) {
				missingPriorities++;
			}else {
				hasPriority.add(goal);
				if(agentPrioMap.containsKey(goal.getColor())) {
					int c = agentPrioMap.get(goal.getColor());
					agentPrioMap.put(goal.getColor(), Math.min(c, currentPriorityMap.get(goal)));
				}else {
					agentPrioMap.put(goal.getColor(), currentPriorityMap.get(goal));
				}
			}
		}
		while(missingPriorities > 0) {
			for(Goal goal : goals) {
				if(hasPriority.contains(goal))
					continue;
				int min = 0;
				if(agentPrioMap.containsKey(goal.getColor())) {
					min = agentPrioMap.get(goal.getColor());
				}
				List<Goal> goalsCrossing = goalPathMap.get(goal);
				int currentPriority = currentPriorityMap.get(goal);
				int updatedPriority = currentPriority;
				for(Goal gc : goalsCrossing) {
					int tempPriority = currentPriorityMap.get(gc);
					if(!goalPathMap.get(gc).contains(goal)) {
						tempPriority++;
					}
					if(tempPriority <= min){
						tempPriority = min+1;
					}
						
					if (tempPriority > updatedPriority) {
						updatedPriority = tempPriority;
					}
				}
				if(updatedPriority <= min){
					updatedPriority = min+1;
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
				}
			}
		}
		
		
		// Add all to priorityMap
		for(Goal goal : goals) {

			int prio = currentPriorityMap.get(goal);
			
			//System.err.println(goal.getLetter() + "  " + prio);
			priorityMap.put(goal.getLocation(), prio);
			taskPriority = Math.max(taskPriority, priorityMap.get(goal.getLocation()));
		}
		
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
		Task task = null;
		List<Task> denied = new LinkedList<>();
		while (task == null && !tasks.isEmpty()) {
			//TODO : If multiple task have same priority pick the one that can be solved the fastest (Look at estimatedTime for task)
			task = tasks.poll();
			if (task.getPriority() < taskPriority || !task.assignAgent(agent)) {
			//if (!task.assignAgent(agent)) {
				denied.add(task);
				task = null;
			}
			if(task != null) {
				List<Task> samePriority = new LinkedList<>();
				Task temp = null;
				while(!tasks.isEmpty() && tasks.peek().getPriority() == task.getPriority()) {
					temp = tasks.poll();
					if(temp.assignAgent(agent)) {
						if(temp.estimatedTime(state) < task.estimatedTime(state)) {
							samePriority.add(task);
							task = temp;
						}else {
							samePriority.add(temp);
						}
					}else {
						samePriority.add(temp);
					}
					
				}
				tasks.addAll(samePriority);
			}
		}
		tasks.addAll(denied);
		return assignTask(state, agent, task);
	}

	private Task assignTask(State state, Agent agent, Task task) {
		Task result = null;
		if (task != null && task.assignAgent(agent)) {
			Planner planner = plannerMap.get(agent.getId());
			if (!planner.addTask(state, task)) {
				planner.clear();

				Task naiveTask = task.getNaive();
				if (naiveTask == null) {
					System.err.println("No naive solution found");
					return null;
				}
				naiveTask.assignAgent(agent);
				State terminalState = planner.createPlan(state, naiveTask);
				if (terminalState == null) {
					addTask(agent.getColor(), task);
					return null;
				}
				List<Location> plan = terminalState.extractLocationPlan(agent);

				int lock = 0;
				Map<Integer,List<Box>> boxesToMove = new HashMap<>();
				Map<Integer,List<Agent>> agentsToMove = new HashMap<>();
				for (Location location : plan) {
					MovableObject object = state.getObjectAt(location);
					if (object instanceof Agent) {
						Agent moveAgent = (Agent) object;
						if(moveAgent.getId() != agent.getId()) {
							if(!agentsToMove.containsKey(moveAgent.getColor())) {
								agentsToMove.put(moveAgent.getColor(), new ArrayList<>());
							}
							agentsToMove.get(moveAgent.getColor()).add(moveAgent);
						}
					}
					else if (object instanceof Box) {
						Box box = (Box) object;
						boolean moveBox = true;
						if (task instanceof BoxTask) {
							BoxTask boxTask = (BoxTask) task;
							moveBox = !boxTask.getBoxes().contains(box);
						}
						if (moveBox) {
							if(!boxesToMove.containsKey(box.getColor())) {
								boxesToMove.put(box.getColor(), new ArrayList<>());
							}
							boxesToMove.get(box.getColor()).add(box);
						}
					}
				}
				for(Integer col : boxesToMove.keySet()) {
					if(agentsToMove.containsKey(col)) {
						Agent agentToMove = agentsToMove.get(col).get(0);
						agentsToMove.get(col).remove(0);
						taskMap.get(col).add(new MoveBoxesAndAgentTask(task.getPriority()+1,task,boxesToMove.get(col), agentToMove,plan));
						lock++;
					}else {
						List<Box> boxes = boxesToMove.get(col);
						if(boxes.size() == 1) {
							Task nextTask = new MoveBoxTask(task.getPriority()+1, task, boxes.get(0), plan);
							taskMap.get(col).add(new MoveToBoxTask(task.getPriority()+2, boxes.get(0), nextTask));
							lock++;
						}else {
							taskMap.get(col).add(new MoveBoxesTask(task.getPriority()+1, task, boxes, plan));
							lock++;
						}
					}
				}
				for(Integer col : agentsToMove.keySet()) {
					for(Agent moveAgent : agentsToMove.get(col)) {
						taskMap.get(moveAgent.getColor()).add(new MoveAgentTask(task.getPriority()+1, task, moveAgent, plan));
						lock++;
					}
				}

				lockTask(task, lock);

				if (!taskLockMap.containsKey(task)) {
					addTask(agent.getColor(), task);
				}
			}
			else {
				result = task;
			}
		}
		return result;
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
				unlockTask(resolved);
				
			}
			
		}
		updatePriority();
	}
	
	private void updatePriority() {
		int prio = Integer.MIN_VALUE;
		for(Integer key : taskMap.keySet()) {
			Queue<Task> prioQueue = taskMap.get(key);
			if (!prioQueue.isEmpty()) {
				if (prio < taskMap.get(key).peek().getPriority()) {
					prio = taskMap.get(key).peek().getPriority();
				}
			}
		}
		for(Integer key : plannerMap.keySet()) {
			Planner p = plannerMap.get(key);
			if(p.getCurrentTask() != null) {
				Task task = p.getCurrentTask();
			    if (prio < task.getPriority()) {
			    	prio = task.getPriority();
				}
			}
		}
		taskPriority = prio;
		
	}

	private boolean agentsWorking(State state) {
		for(Agent agent : state.getAgents()) {
			Planner planner = plannerMap.get(agent.getId());
			if(planner.getCurrentTask() != null) {
				return true;
			}
		}
		return false;
	}

	private void addConflict(Map<Location, Set<MovableObject>> conflictMap, MovableObject object, Location location) {
		Set<MovableObject> conflictList = conflictMap.computeIfAbsent(location, k -> new HashSet<>());
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
		int noOpCounter = 0;
		long timeStart = System.currentTimeMillis();
		while (!solved) {
			boolean done = true;

			String cmd = "";
			for (Agent agent : state.getAgents()) {
				Planner planner = getPlanner(agent);
				if (planner.isEmpty()) {
					do {
						Task nextTask = planner.getNextTask();
						completeTasks(planner);
						if (nextTask != null) {
							assignTask(state, agent, nextTask);
						}
						else {
							assignTask(state, agent);
						}
					}
					while (planner.isEmpty() && !planner.getTasks().isEmpty());
				}
				if (!planner.isEmpty()) {
					done = false;
				}
				Action a = planner.poll();
				if(!(a instanceof NoOpAction)) {
					noOpCounter = 0;
				}
				cmd += a.toString() + ";";
			}
			noOpCounter++;
			solved = done && allTasksCompleted();
			if(!agentsWorking(state)) {
				updatePriority();
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

			//System.err.println("RESPONSE: " + message);

			String[] feedback = message.split(";");
			Map<Location, Set<MovableObject>> conflicts = new HashMap<>();
			for (Agent agent : state.getAgents()) {
				boolean error = !Boolean.parseBoolean(feedback[agent.getId()]);
				Planner planner = getPlanner(agent);
				Action action = planner.getLastAction();
				if (error) {
					//System.err.println("Conflict involving Agent"+agent.getId());
					collectConflicts(conflicts, agent, action);
				}
				else {
					int oldGoalCount = state.boxesInGoal();
					Location oldAgentLoc = state.getAgent(agent).getLocation();
					state.applyAction(agent, action);
					Location newAgentLoc = state.getAgent(agent).getLocation();
					int newGoalCount = state.boxesInGoal();
					
					if(newGoalCount > oldGoalCount) {
						calculateSafeLocations(state);
						State.freeBoxes--;
						State.freeGoals-= Math.abs(newGoalCount - oldGoalCount)/2;
					}
					if(!oldAgentLoc.equals(newAgentLoc) && State.goalMap.containsKey(oldAgentLoc) && State.goalMap.get(oldAgentLoc) instanceof AgentGoal) {
						if(State.goalMap.get(oldAgentLoc).getLetter() == agent.getLetter()) {
							addTask(agent.getColor(), new AgentToGoalTask(priorityMap.get(oldAgentLoc),State.goalMap.get(oldAgentLoc), agent));
							State.freeBoxes++;
							State.freeGoals++;
						}
		
					}
					if(action instanceof BoxAction) {
						BoxAction boxAction = (BoxAction) action;
						Dir boxDir = boxAction.getBoxDirection();

						Location newBoxLoc;
						newBoxLoc = (action instanceof PushAction) ? new Location(newAgentLoc.getRow() + Action.dirToRowChange(boxDir), newAgentLoc.getCol() + Action.dirToColChange(boxDir)) : new Location(oldAgentLoc);
						Location oldBoxLoc;
						oldBoxLoc = (action instanceof PushAction) ? new Location(newAgentLoc) : new Location(newBoxLoc.getRow() + Action.dirToRowChange(boxDir),newBoxLoc.getCol() + Action.dirToColChange(boxDir));
						Box box = state.getBoxAt(newBoxLoc);
						int boxRow = box.getLocation().getRow();
						int boxCol = box.getLocation().getCol();
						if(State.goalMap.containsKey(oldBoxLoc)){
							Goal goal = State.goalMap.get(oldBoxLoc);

							if(goal.getAssignedObj().equals(box)) {
								addGoalTask(goal);
								State.freeBoxes++;
								State.freeGoals++;
							}
						}


						if(planner.isEmpty() && newGoalCount > oldGoalCount) {
							if (box.isSafe()) {
								State.walls[boxRow][boxCol] = true;
								State.convertedBoxes++;
								state.boxes.remove(box.getId());

								state.updateDistanceMaps();
							}
						}
					}
				}
			}

			Set<Agent> resolved = new HashSet<>();
			for (Location location : conflicts.keySet()) {
				Set<Agent> agents = conflicts.get(location).stream()
						.filter(x -> !resolved.contains(x))
						.filter(x -> x instanceof Agent)
						.map(x -> (Agent) x)
						.collect(Collectors.toSet());
				if (agents.isEmpty()) continue;
				Agent prio = agents.stream().max(new Comparator<Agent>() {
					@Override
					public int compare(Agent o1, Agent o2) {
						Task t1 = getPlanner(o1).getCurrentTask();
						Task t2 = getPlanner(o2).getCurrentTask();
						int p1 = t1 == null ? Integer.MIN_VALUE : t1.getPriority();
						int p2 = t2 == null ? Integer.MIN_VALUE : t2.getPriority();
						return p1 - p2;
					}
				}).get();
				for (Agent agent : agents) {
					Planner planner = getPlanner(agent);
					addTasks(agent.getColor(), planner.getTasks());
					planner.clear();
					
					assignTask(state, agent);
					if (!agent.equals(prio)) {
						planner.addDelay();
					}
					resolved.add(agent);
				}
			}
			if(solved && State.freeGoals != 0) {
				if(!taskLockMap.keySet().isEmpty()) {
					for(Task task : taskLockMap.keySet()) {
						addTask(task.getAgent().getColor(), task);
					}

					updatePriority();
					solved = false;
				}
				
			}
			System.err.println(State.freeGoals);

			if(noOpCounter >= 3) {
				for(Integer color : taskMap.keySet()) {
					taskMap.get(color).clear();
				}
				for (Agent agent : state.getAgents()) {
					Planner planner = getPlanner(agent);
					planner.clear();
				}
				for (Goal goal : state.getGoals()) {
					if(goal instanceof AgentGoal) {
						AgentToGoalTask gt = new AgentToGoalTask(goal, (Agent) goal.getAssignedObj());
						if(!gt.isTerminal(state))
							addGoalTask(goal);
					}else {
						GoalTask gt = new GoalTask((Box) goal.getAssignedObj(), goal);
						if(!gt.isTerminal(state))
							addGoalTask(goal);
					}
					
				}
				updatePriority();
			}
			
			solved = solved || State.freeGoals == 0;
		}
		double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
		//System.err.println("Time spent on solving: " + timeSpent + " seconds.");
	}

}
