package Action;

public class PushAction extends Action {

    public PushAction(Dir dir1, Dir dir2) {
        this.dir1 = dir1;
        this.dir2 = dir2;
    }

    public String toString() {
        return String.format("Push(%s,%s);", dir1, dir2);
    }
}
