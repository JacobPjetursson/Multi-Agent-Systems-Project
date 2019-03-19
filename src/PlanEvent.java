public class PlanEvent extends Event {
    private Box box;
    private Goal goal;

    public PlanEvent(Box box, Goal goal) {
        this.box = box;
        this.goal = goal;
    }
}
