package action;

import state.State;

public class NoOpAction extends Action {

    public String toString() {
        return "NoOp";
    }

    @Override
    public void apply(State state) {

    }
}
