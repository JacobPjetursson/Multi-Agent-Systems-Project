package action;

public class PullAction extends Action {

    public PullAction(Dir dir1, Dir dir2) {
        this.dir1 = dir1;
        this.dir2 = dir2;
    }

    public String toString() {
        return String.format("Pull(%s,%s)", dir1, dir2);
    }
}
