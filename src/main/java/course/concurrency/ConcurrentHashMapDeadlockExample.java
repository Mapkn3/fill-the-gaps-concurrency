package course.concurrency;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ConcurrentHashMapDeadlockExample {
    public static void main(String[] args) {
        var first = new PredefinedHashCode(1);
        var second = new PredefinedHashCode(2);

        var map = new ConcurrentHashMap<>(
                Map.of(
                        first, "first",
                        second, "second"
                )
        );

        var computeFuture = CompletableFuture.runAsync(
                () -> map.compute(
                        first,
                        (firstKey, firstValue) -> map.compute(second, (secondKey, secondValue) -> "first|second")
                )
        );
        var computeFuture1 = CompletableFuture.runAsync(
                () -> map.compute(
                        second,
                        (secondKey, secondValue) -> map.compute(first, (firstKey, firstValue) -> "second|first")
                )
        );
        CompletableFuture.allOf(computeFuture, computeFuture1).join();
    }

    static class PredefinedHashCode {
        private final int hash;

        PredefinedHashCode(int hash) {
            this.hash = hash;
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }
}
