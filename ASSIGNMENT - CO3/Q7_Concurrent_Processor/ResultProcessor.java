import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class ResultProcessor {
    static class Result {
        final String file;
        final int score;
        Result(String file, int score) { this.file = file; this.score = score; }
    }

    static class ProcessingFailure extends Exception {
        ProcessingFailure(String message, Throwable cause) { super(message, cause); }
    }

    static class ScoreTask implements Callable<Result> {
        private final Path file;
        ScoreTask(Path file) { this.file = file; }

        @Override
        public Result call() throws ProcessingFailure {
            try {
                List<String> lines = Files.readAllLines(file);
                if (lines.isEmpty()) throw new IOException("Empty score file");
                int score = Integer.parseInt(lines.get(0).trim());
                if (score < 0 || score > 100) throw new IOException("Score outside 0..100");
                return new Result(file.getFileName().toString(), score);
            } catch (IOException | NumberFormatException e) {
                throw new ProcessingFailure("Could not process " + file.getFileName(), e);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        Path dir = args.length > 0 ? Paths.get(args[0]) : Paths.get("Q7_Concurrent_Processor", "input");
        Files.createDirectories(dir);
        // Deterministic demo inputs. One invalid file and one missing path are handled separately.
        Files.write(dir.resolve("student01.txt"), Arrays.asList("85"));
        Files.write(dir.resolve("student02.txt"), Arrays.asList("92"));
        Files.write(dir.resolve("student03.txt"), Arrays.asList("not-a-number"));
        Files.write(dir.resolve("student04.txt"), Arrays.asList("120"));

        int poolSize = 4; // Repeat with 1, 2, 4 and 8; record your own elapsed times.
        ExecutorService pool = Executors.newFixedThreadPool(poolSize);
        ConcurrentMap<String, Integer> successes = new ConcurrentHashMap<>();
        ConcurrentMap<String, String> failures = new ConcurrentHashMap<>();
        long start = System.nanoTime();

        try (Stream<Path> paths = Files.list(dir)) {
            List<Future<Result>> futures = new ArrayList<>();
            paths.filter(p -> p.toString().endsWith(".txt"))
                 .forEach(p -> futures.add(pool.submit(new ScoreTask(p))));

            for (Future<Result> future : futures) {
                try {
                    Result result = future.get();
                    successes.put(result.file, result.score);
                } catch (ExecutionException e) {
                    Throwable cause = e.getCause();
                    failures.put("task-" + failures.size(), cause.getMessage());
                }
            }
        } finally {
            pool.shutdown();
            if (!pool.awaitTermination(1, TimeUnit.MINUTES)) pool.shutdownNow();
        }

        // Explicitly demonstrate a missing-file failure as a separate task.
        ExecutorService missingPool = Executors.newSingleThreadExecutor();
        try {
            Future<Result> missing = missingPool.submit(new ScoreTask(dir.resolve("missing.txt")));
            try {
                missing.get();
            } catch (ExecutionException e) {
                failures.put("missing.txt", e.getCause().getMessage());
            }
        } finally {
            missingPool.shutdown();
            missingPool.awaitTermination(1, TimeUnit.MINUTES);
        }

        long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        System.out.println("Successful files: " + successes.size());
        System.out.println("Failed files: " + failures.size());
        System.out.println("Success details: " + successes);
        System.out.println("Failure details: " + failures);
        System.out.println("Elapsed milliseconds: " + elapsedMs);
    }
}
