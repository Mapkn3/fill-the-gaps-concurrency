package course.concurrency.exams.blocking_queue;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import static java.lang.Thread.State.WAITING;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomBlockingQueueTest {

    public static final long AWAIT_MILLIS = 1000L;

    private Thread startNewThread(Runnable runnable) {
        var thread = new Thread(runnable);
        thread.start();

        return thread;
    }

    @Test
    void enqueueSuccessful() {
        var queue = new CustomBlockingQueue<>(1);
        queue.enqueue(new Object());

        assertEquals(1, queue.size());
    }

    @Test
    void dequeueSuccessful() {
        var queue = new CustomBlockingQueue<>(1);
        var item = new Object();
        queue.enqueue(item);
        var dequeuedItem = queue.dequeue();

        assertEquals(item, dequeuedItem);
        assertEquals(0, queue.size());
    }

    @Test
    void secondEnqueueIsBlocked() throws InterruptedException {
        var queue = new CustomBlockingQueue<>(1);
        queue.enqueue(new Object());

        assertEquals(1, queue.size());

        var secondEnqueueThread = startNewThread(() -> queue.enqueue(new Object()));
        Thread.sleep(AWAIT_MILLIS);

        assertEquals(WAITING, secondEnqueueThread.getState());
        assertEquals(1, queue.size());
    }

    @Test
    void onlyDequeueIsBlocked() throws InterruptedException {
        var queue = new CustomBlockingQueue<>(1);
        var dequeueThread = startNewThread(queue::dequeue);
        Thread.sleep(AWAIT_MILLIS);

        assertEquals(WAITING, dequeueThread.getState());
        assertEquals(0, queue.size());
    }

    @Test
    void queueShouldWorkAsFIFO() {
        var queue = new CustomBlockingQueue<Integer>(3);
        queue.enqueue(1);
        queue.enqueue(2);
        queue.enqueue(3);

        assertEquals(3, queue.size());
        assertEquals(1, queue.dequeue());
        assertEquals(2, queue.dequeue());
        assertEquals(3, queue.dequeue());
        assertEquals(0, queue.size());
    }

    @Test
    void shouldWorkInMultithreading() throws InterruptedException {
        var executorService = Executors.newCachedThreadPool();
        var queue = new CustomBlockingQueue<Integer>(100);
        var countOfItems = 1_000;
        var countDownLatch = new CountDownLatch(1);
        for (int i = 0; i < countOfItems; i++) {
            executorService.submit(() -> {
                try {
                    countDownLatch.await();
                    var dequeuedItem = queue.dequeue();
                    assertTrue(dequeuedItem >= 0 && dequeuedItem < countOfItems);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        for (int i = 0; i < countOfItems; i++) {
            var item = i;
            executorService.submit(() -> {
                try {
                    countDownLatch.await();
                    queue.enqueue(item);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        countDownLatch.countDown();
        executorService.shutdown();

        assertTrue(executorService.awaitTermination(AWAIT_MILLIS, MILLISECONDS));
        assertEquals(0, queue.size());

    }
}