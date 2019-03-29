package action;

public class MoveAction extends Action {
	private Dir dir;

    public MoveAction(Dir dir) {
        this.dir = dir;
    }
    
	public Dir getDirection() {
		return dir;
	}

    @Override
    public String toString() {
        return String.format("Move(%s)", dir);
    }
}
