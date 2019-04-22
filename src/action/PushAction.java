package action;

public class PushAction extends BoxAction {

    PushAction(Dir agentDir, Dir boxDir) {
		super(agentDir, boxDir);
	}

    @Override
	public String toString() {
    	return "Push" + super.toString();
    }
}
