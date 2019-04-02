package action;

public class NoOpAction extends Action {
	public static final String COMMAND = "NoOp";
	
	@Override
    public String toString() {
        return COMMAND;
    }
    
}
