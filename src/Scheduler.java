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

	private static Map<Location,Integer> priorityMap;
	private Set<Goal> usedGoalsSet; // Used for monitoring which goals have been made to tasks

	public Scheduler(State initialState, BufferedReader serverMessages) {
		this.serverMessages = serverMessages;
		// Get initial plan from initial state, queue them to priorityqueue
		state = initialState;

		plannerMap = new HashMap<>();
		taskMap = new HashMap<>();
		taskLockMap = new HashMap<>();
		usedGoalsSet = new HashSet<>();

		Comparator<Task> taskComparator = (t1, t2) -> t2.getPriority() - t1.getPriority();

		for (Agent agent : state.getAgents()) {
			plannerMap.put(agent.getId(), new Planner(agent.getId()));
			taskMap.put(agent.getColor(), new PriorityQueue<>(taskComparator));
		}

		state.assignBoxesToGoals();
		priorityMap = new HashMap<>();
		calculateGoalPriorities();
		State.totalGoals = state.getBoxes().size();
		State.freeBoxes = State.totalGoals;
		calculateSafeLocations(state);

		// Task of getting box to goal
		for (Goal goal : state.getGoals()) {
			if (!usedGoalsSet.contains(goal))
				addGoalTask(goal);
		}

		for (Goal goal : state.getAgentGoals()) {
			for (Agent agent : state.getAgents()) {
				if (goal.getLetter() == (char)(agent.getId()+'0')) {
					addTask(goal.getColor(), new AgentToGoalTask(priorityMap.get(goal.getLocation()),goal, agent));
				}
			}
		}
		
		// Initial tasks
		// TODO - prioritize which agent takes which task, instead of random
		for (Agent agent : state.getAgents()) {
			assignTask(state, agent);
		}
	}

	private void calculateSafeLocations(State state) {
		List<Goal> goals = new ArrayList<>(state.getGoals());
		goals.addAll(state.getAgentGoals());
		Map<Location,Integer> paths = new HashMap<>();
		for(Goal goal : goals) {
			if(state.getBoxAt(goal.getLocation()) != null) {
				continue;
			}
			Location location;
			if(goal.getLetter() >= '0' && goal.getLetter() <= '9') {
				location = state.getAgent(Character.getNumericValue(goal.getLetter())).getLocation();
			}else {
				location = state.getBox(goal.getAssignedBox()).getLocation();
			}
			List<Location> shortestPath = state.getPath(location, goal.getLocation());
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
					if(paths.containsKey(location) && safeValue == 0) {
						safeValue = paths.get(location);
					}
					State.safeLocation.put(location, Math.min((int) (safeValue*(State.freeBoxes)),State.freeBoxes*State.freeBoxes));
				}else {
					State.safeLocation.put(location, -1);
				}
			}
		}
	}

	private void addGoalTask(Goal goal) {
		Box box = goal.getAssignedBox();
		int priority = priorityMap.get(goal.getLocation());
		GoalTask goalTask = new GoalTask(priority, box, goal);
		MoveToBoxTask moveTask = new MoveToBoxTask(priority, box, goalTask);
		addTask(goal.getColor(), moveTask);
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
		//TODO : Agents move to goal should be lower than all its other tasks
		int size = state.getGoals().size()+state.getAgentGoals().size();
		priorityMap = new HashMap<>();
		Map<Goal, Integer> currentPriorityMap = new HashMap<>();
		Map <Goal, List<Goal>> goalPathMap = new HashMap<>();
		List<Goal> goals = new ArrayList<>(state.getGoals());
		goals.addAll(state.getAgentGoals());
		for(Goal goal : goals) {
			goalPathMap.put(goal, new ArrayList<>());
			Location location;
			if(goal.getLetter() >= '0' && goal.getLetter() <= '9') {
				location = state.getAgent(Character.getNumericValue(goal.getLetter())).getLocation();
			}else {
				location = goal.getAssignedBox().getLocation();
			}
			List<Location> shortestPath = state.getPath(location, goal.getLocation());
			for(Location l : shortestPath) {
				if(State.goalMap.containsKey(l)) {
					List<Goal> goalsCrossing = goalPathMap.get(goal);
					goalsCrossing.add(State.goalMap.get(l));
					goalPathMap.put(goal, goalsCrossing);
				}else if(State.agentGoalMap.containsKey(l)) {
					List<Goal> goalsCrossing = goalPathMap.get(goal);
					goalsCrossing.add(State.agentGoalMap.get(l));
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
			task = tasks.poll();
			if (!task.assignAgent(agent)) {
				denied.add(task);
				task = null;
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
						if(box.getColor()!=agent.getColor()) {
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
					int oldGoalCount = state.boxesInGoal();
					Location oldAgentLoc = agent.getLocation();
					state.applyAction(agent, action);
					Location newAgentLoc = state.getAgent(agent).getLocation();
					int newGoalCount = state.boxesInGoal();
					if(newGoalCount > oldGoalCount) {
						calculateSafeLocations(state);
						State.freeBoxes--;
					}
					if(!oldAgentLoc.equals(newAgentLoc) && State.agentGoalMap.containsKey(oldAgentLoc)) {
						addTask(agent.getColor(), new AgentToGoalTask(priorityMap.get(oldAgentLoc),State.agentGoalMap.get(oldAgentLoc), agent));
						State.freeBoxes++;
					}
					//If box moved away from goal add goalTask again
					if(action instanceof BoxAction) {
						BoxAction boxAction = (BoxAction) action;
						Dir boxDir = boxAction.getBoxDirection();

						Location newBoxLoc;
						newBoxLoc = (action instanceof PushAction) ? new Location(newAgentLoc.getRow() + Action.dirToRowChange(boxDir), newAgentLoc.getCol() + Action.dirToColChange(boxDir)) : new Location(oldAgentLoc);
						Location oldBoxLoc;
						oldBoxLoc = (action instanceof PushAction) ? new Location(newAgentLoc) : new Location(newBoxLoc.getRow() + Action.dirToRowChange(boxDir),newBoxLoc.getCol() + Action.dirToColChange(boxDir));
						if(State.goalMap.containsKey(oldBoxLoc)){
							Goal goal = State.goalMap.get(oldBoxLoc);
							if(goal.getLetter() == state.getBoxAt(newBoxLoc).getLetter()) {
								System.err.println("Re-adding goal task for goal " + goal.getLetter());
								addGoalTask(goal);
								State.freeBoxes++;
								break;
							}
						}
					}
				}
			}

			// TODO - handle conflicts better
			Set<MovableObject> resolved = new HashSet<>();
			for (Location location : conflicts.keySet()) {
				Set<MovableObject> objects = conflicts.get(location).stream()
						.filter(x -> !resolved.contains(x))
						.collect(Collectors.toSet());
				Optional<MovableObject> temp = objects.stream().filter(x -> x instanceof Agent).findAny();
				if (!temp.isPresent()) continue;
				Agent priority = (Agent) temp.get();
				Planner priorityPlanner = getPlanner(priority);
				priorityPlanner.undo();
				Set<MovableObject> rest = objects.stream().filter(x -> !x.equals(priority)).collect(Collectors.toSet());
				for (MovableObject object : rest) {
					if (object instanceof Agent) {
						Agent agent = (Agent) object;
						Planner planner = getPlanner(agent);
						addTasks(agent.getColor(), planner.getTasks());
						planner.clear();
						planner.addTask(state, new AvoidConflictTask(1, priority.getId(), priorityPlanner.getPlan()));
					}
					else if (object instanceof Box) {
						Box box = (Box) object;
						Task task = priorityPlanner.getTasks().peek();
						Task avoidTask = new MoveBoxTask(task.getPriority() + 1, task, box, priorityPlanner.getPath(state));
						taskMap.get(box.getColor()).add(avoidTask);
						lockTask(task, 1);
						priorityPlanner.clear();
					}
					resolved.add(object);
				}
				resolved.add(priority);
			}
		}
		double timeSpent = (System.currentTimeMillis() - timeStart) / 1000.0;
		System.err.println("Time spent on solving: " + timeSpent + " seconds.");
	}
}
