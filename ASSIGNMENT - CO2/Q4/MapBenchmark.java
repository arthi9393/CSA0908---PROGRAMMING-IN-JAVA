
import java.util.*;

public class MapBenchmark {
    public static void main(String[] args) {

        int n = 100000;

        HashMap<Integer, String> hashMap = new HashMap<>();
        TreeMap<Integer, String> treeMap = new TreeMap<>();
        LinkedHashMap<Integer, String> linkedHashMap = new LinkedHashMap<>();

        long start = System.nanoTime();
        for (int i = 0; i < n; i++)
            hashMap.put(i, "Student" + i);
        long hashTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < n; i++)
            treeMap.put(i, "Student" + i);
        long treeTime = System.nanoTime() - start;

        start = System.nanoTime();
        for (int i = 0; i < n; i++)
            linkedHashMap.put(i, "Student" + i);
        long linkedTime = System.nanoTime() - start;

        System.out.println("Number of entries: " + n);
        System.out.println("HashMap time: " + hashTime + " ns");
        System.out.println("TreeMap time: " + treeTime + " ns");
        System.out.println("LinkedHashMap time: " + linkedTime + " ns");

        System.out.println("HashMap size: " + hashMap.size());
        System.out.println("TreeMap size: " + treeMap.size());
        System.out.println("LinkedHashMap size: " + linkedHashMap.size());

        System.out.println("\nHashMap: " + hashMap.get(10));
        System.out.println("TreeMap first key: " + treeMap.firstKey());
        System.out.println("LinkedHashMap first key: "
                + linkedHashMap.keySet().iterator().next());
    }
}
