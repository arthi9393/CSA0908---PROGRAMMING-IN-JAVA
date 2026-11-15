
public class StringBenchmark {
    public static void main(String[] args) {
        int n = 10000;

        long start = System.nanoTime();
        String s = "";
        for (int i = 0; i < n; i++) {
            s = s + "Java";
        }
        long stringTime = System.nanoTime() - start;

        start = System.nanoTime();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append("Java");
        }
        long builderTime = System.nanoTime() - start;

        start = System.nanoTime();
        StringBuffer sf = new StringBuffer();
        for (int i = 0; i < n; i++) {
            sf.append("Java");
        }
        long bufferTime = System.nanoTime() - start;

        System.out.println("Iterations: " + n);
        System.out.println("String time: " + stringTime + " ns");
        System.out.println("StringBuilder time: " + builderTime + " ns");
        System.out.println("StringBuffer time: " + bufferTime + " ns");

        System.out.println("String length: " + s.length());
        System.out.println("StringBuilder length: " + sb.length());
        System.out.println("StringBuffer length: " + sf.length());
    }
}
