
import java.util.*;

public class ListBenchmark {
    public static void main(String[] args) {
        int n = 100000;

        ArrayList<Integer> arrayList = new ArrayList<>();
        LinkedList<Integer> linkedList = new LinkedList<>();
        ArrayDeque<Integer> arrayDeque = new ArrayDeque<>();

        long start = System.nanoTime();
        for (int i = 0; i < n; i++) arrayList.add(i);
        long arrayTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < n; i++) linkedList.add(i);
        long linkedTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < n; i++) arrayDeque.add(i);
        long dequeTime = System.nanoTime() - start;

        System.out.println("Number of elements: " + n);
        System.out.println("ArrayList time: " + arrayTime + " ns");
        System.out.println("LinkedList time: " + linkedTime + " ns");
        System.out.println("ArrayDeque time: " + dequeTime + " ns");

        System.out.println("ArrayList size: " + arrayList.size());
        System.out.println("LinkedList size: " + linkedList.size());
        System.out.println("ArrayDeque size: " + arrayDeque.size());
    }
}
