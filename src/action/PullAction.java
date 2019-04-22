package action;

public class PullAction extends BoxAction {

    PullAction(Dir agentDir, Dir boxDir) {
		super(agentDir, boxDir);
	}

    @Override
	public String toString() {
    	return "Pull" + super.toString();
    }
}
