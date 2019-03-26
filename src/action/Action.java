package action;

public abstract class Action {
    Dir dir1, dir2;

    public enum Dir {
        N, S, E, W
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
}
