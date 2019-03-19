package Action;

public class PullAction extends Action {

    public PullAction(Dir dir1, Dir dir2) {
        this.dir1 = dir1;
        this.dir2 = dir2;
    }

    public String toString() {
        return "Pull(" + dir1.name() + "," + dir2.name() + ");";
    }
}
