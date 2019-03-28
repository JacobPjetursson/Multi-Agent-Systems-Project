package action;

import java.util.ArrayList;

public abstract class Action {
    public Dir dir1;
    public Dir dir2;

    public enum Dir {
        N, W, E, S
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
        if (d == null)
            return 0;

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
        if (d == null)
            return 0;

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
