package state;

public class BoxGoal extends Goal {

    private Box assignedBox;

    public BoxGoal(Location position, int color, char letter) {
        super(position, color, letter);
    }

    @Override
    public void assignObj(MovableObject obj) {
        assignedBox = (Box) obj;
    }

    @Override
    public MovableObject getAssignedObj() {
        return assignedBox;
    }
}
