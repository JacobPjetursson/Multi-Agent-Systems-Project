package task;

import java.util.LinkedList;

import java.util.List;

import state.*;

public class AgentToGoalTask extends Task {
	
	private Goal goal;
	private Agent agent;
	
	public AgentToGoalTask(Goal goal, Agent agent) {
		this(-1,goal,agent);
	}

	public AgentToGoalTask(int priority, Goal goal, Agent agent) {
		super(priority);
		this.agent = agent;
		this.goal = goal;
	}
	
	public Goal getGoal() {
		return goal;
	}
	
	public Agent getAgent() {
		return agent;
	}

	@Override
	public boolean isTerminal(State state) {
		return state.getAgent(agent).getLocation().equals(goal.getLocation());
	}

	@Override
	public int h(State state) {
		DistanceMap dm = State.DISTANCE_MAPS.get(goal.getLocation());
		return dm.distance(state.getAgent(agent).getLocation());
	}

	@Override
	public boolean updateState(State state) {
		return true;
	}

	@Override
	public void initializeState(State state) {
		
	}

	@Override
	public Location getGoalLocation() {
		return goal.getLocation();
	}

	@Override
	public Task getNaive() {
		return new NaiveAgentToGoalTask(this);
	}

	@Override
	public Task getNextTask() {
		return null;
	}
	
	public boolean assignAgent(Agent agent) {
		if(this.agent.equals(agent)) {
			return super.assignAgent(agent);
		}
    	return false;
    }
	
	private static class NaiveAgentToGoalTask extends AgentToGoalTask {		
		public NaiveAgentToGoalTask(AgentToGoalTask task) {
			super(task.getPriority(), task.getGoal(), task.getAgent());
		}

		@Override
		public void initializeState(State state) {
			List<StateObject> preserve = new LinkedList<>();
			preserve.add(getAgent());
			state.removeObjectsExcept(preserve);
		}
	}

	
	
}