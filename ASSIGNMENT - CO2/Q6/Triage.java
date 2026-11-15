
import java.util.*;

class Patient {
    String name;
    int priority;
    int arrivalOrder;

    Patient(String name, int priority, int arrivalOrder) {
        this.name = name;
        this.priority = priority;
        this.arrivalOrder = arrivalOrder;
    }

    @Override
    public String toString() {
        return name + " (Priority: " + priority + ")";
    }
}

public class Triage {
    public static void main(String[] args) {

        Comparator<Patient> comparator =
            Comparator.comparingInt((Patient p) -> p.priority)
                      .thenComparingInt(p -> p.arrivalOrder);

        PriorityQueue<Patient> queue =
            new PriorityQueue<>(comparator);

        queue.add(new Patient("Arun", 2, 1));
        queue.add(new Patient("Priya", 1, 2));
        queue.add(new Patient("Ravi", 2, 3));
        queue.add(new Patient("Kumar", 3, 4));
        queue.add(new Patient("Meena", 1, 5));

        System.out.println("PATIENT TREATMENT ORDER:");

        while (!queue.isEmpty()) {
            System.out.println(queue.poll());
        }
    }
}

