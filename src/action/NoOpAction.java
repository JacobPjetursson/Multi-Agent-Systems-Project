package action;

public class NoOpAction extends Action {
	private static final String COMMAND = "NoOp";
	
	@Override
    public String toString() {
        return COMMAND;
    }
    
}
