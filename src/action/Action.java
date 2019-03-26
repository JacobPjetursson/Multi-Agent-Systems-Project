package action;

import state.State;

public abstract class Action {
    Dir dir1, dir2;

    public enum Dir {
        N, S, E, W
    }

    public abstract String toString();

    public abstract void apply(State state);
}
