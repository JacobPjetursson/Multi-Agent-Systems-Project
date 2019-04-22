package action;

public class BoxAction extends Action {
	private Dir agentDir;
	private Dir boxDir;
	
	BoxAction(Dir agentDir, Dir boxDir) {
		this.agentDir = agentDir;
		this.boxDir = boxDir;
	}
	
	public Dir getAgentDirection() {
		return agentDir;
	}
	
	public Dir getBoxDirection() {
		return boxDir;
	}

	@Override
	public String toString() {
		return String.format("(%s,%s)", agentDir, boxDir);
	}

}
