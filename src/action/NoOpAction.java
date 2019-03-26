package action;

import state.State;

public class NoOpAction extends Action {
	public static final String COMMAND = "NoOp";

    public String toString() {
        return COMMAND;
    }

    @Override
    public void apply(State state) {

    }

}
