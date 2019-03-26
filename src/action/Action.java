package action;

import state.State;

import java.util.ArrayList;

public abstract class Action {
    public Dir dir1;
    public Dir dir2;

    public enum Dir {
        N, S, E, W
    }

    public static final Action[] EVERY;
    static {
        ArrayList<Action> cmds = new ArrayList<>();
        for (Dir d1 : Dir.values()) {
            for (Dir d2 : Dir.values()) {
                if (!Action.isOpposite(d1, d2)) {
                    cmds.add(new PushAction(d1, d2));
                }
            }
        }
        for (Dir d1 : Dir.values()) {
            for (Dir d2 : Dir.values()) {
                if (d1 != d2) {
                    cmds.add(new PullAction(d1, d2));
                }
            }
        }
        for (Dir d : Dir.values()) {
            cmds.add(new MoveAction(d));
        }
        cmds.add(new NoOpAction());

        EVERY = cmds.toArray(new Action[0]);
    }

    public abstract String toString();
    public abstract void apply(State state);

    @Override
    public boolean equals(Object obj) {
    	if (obj instanceof Action) {
			Action act = (Action) obj;
			return this.toString().equals(act.toString());
		}
    	return false;
    }

    private static boolean isOpposite(Dir d1, Dir d2) {
        return d1.ordinal() + d2.ordinal() == 3;
    }

    public static int dirToRowChange(Dir d) {
        // South is down one row (1), north is up one row (-1).
        switch (d) {
            case S:
                return 1;
            case N:
                return -1;
            default:
                return 0;
        }
    }

    public static int dirToColChange(Dir d) {
        // East is right one column (1), west is left one column (-1).
        switch (d) {
            case E:
                return 1;
            case W:
                return -1;
            default:
                return 0;
        }
    }
}
