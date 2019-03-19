package Action;

public class MoveAction extends Action{

    public MoveAction(Dir dir1) {
        this.dir1 = dir1;
    }

    public String toString() {
        return "Move(" + dir1.name() + ");";
    }
}
