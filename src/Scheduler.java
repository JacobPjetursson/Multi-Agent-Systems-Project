import Event.Event;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Scheduler {
    PriorityQueue<Event> queue;

    public Scheduler() {
        queue = new PriorityQueue<>(new EventComparator());
    }


    private class EventComparator implements Comparator<Event> {
        @Override
        public int compare(Event e1, Event e2) {
            if (e1.getPriority() > e2.getPriority())
                return 1;
            else if (e2.getPriority() > e1.getPriority())
                return -1;
            return 0;
        }
    }
}
