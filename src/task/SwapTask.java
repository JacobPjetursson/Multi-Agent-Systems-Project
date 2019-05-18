package task;

import state.Agent;
import state.Box;
import state.DistanceMap;
import state.Location;
import state.State;

public class SwapTask extends Task {
	
	private Agent agent;
	private Box box;
	private Location agentGoal;
	private Location boxGoal;

	public SwapTask(int priority, Box box, Agent agent) {
		super(priority);
		this.box = box;
		this.agent = agent;
		agentGoal = new Location(box.getLocation());
		boxGoal = new Location(agent.getLocation());
	}

	@Override
	public boolean isTerminal(State state) {
		if(agentGoal.equals(state.getAgent(agent).getLocation()) && boxGoal.equals(state.getBox(box).getLocation())) {
			return true;
		}
		return false;
	}

	@Override
	public int h(State state) {
		DistanceMap boxDM = State.DISTANCE_MAPS.get(boxGoal);
		DistanceMap agentDM = State.DISTANCE_MAPS.get(agentGoal);
		return boxDM.distance(state.getBox(box).getLocation()) + agentDM.distance(state.getAgent(agent).getLocation());
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
		return null;
	}

	@Override
	public boolean assignAgent(Agent agent) {
		if(agent.equals(this.agent)) {
			return super.assignAgent(agent);
		}
		return false;
		
	}

	@Override
	public Task getNaive() {
		return null;
	}

	@Override
	public Task getNextTask() {
		return null;
	}
}
