package course.concurrency.exams.auction;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;

public class Notifier {
    private final ExecutorService executor;

    public Notifier(ExecutorService executor) {
        this.executor = executor;
    }

    public Notifier() {
        this(ForkJoinPool.commonPool());
    }

    public CompletableFuture<Void> sendOutdatedMessage(Bid bid) {
        return CompletableFuture.runAsync(this::imitateSending, executor);
    }

    private void imitateSending() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
