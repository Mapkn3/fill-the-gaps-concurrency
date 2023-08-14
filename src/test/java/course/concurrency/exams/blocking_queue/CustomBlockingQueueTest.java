package course.concurrency.exams.blocking_queue;

import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.Executors;

import static java.lang.Thread.State.WAITING;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomBlockingQueueTest {

    public static final long AWAIT_MILLIS = 1000L;

    private Thread startNewThread(Runnable runnable) {
        var thread = new Thread(runnable);
        thread.start();

        return thread;
    }

    @Test
    void enqueueSuccessful() throws InterruptedException {
        var queue = new CustomBlockingQueue<>(1);
        var enqueueThread = startNewThread(() -> {
            try {
                queue.enqueue(new Object());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        enqueueThread.join(AWAIT_MILLIS);

        assertNotEquals(WAITING, enqueueThread.getState());
        assertEquals(1, queue.size());
    }

    @Test
    void dequeueSuccessful() throws InterruptedException {
        var queue = new CustomBlockingQueue<>(1);
        var item = new Object();
        var enqueueThread = startNewThread(() -> {
            try {
                queue.enqueue(item);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        enqueueThread.join(AWAIT_MILLIS);

        assertNotEquals(WAITING, enqueueThread.getState());
        assertEquals(1, queue.size());

        var dequeueThread = startNewThread(() -> {
            try {
                var dequeuedItem = queue.dequeue();
                assertEquals(item, dequeuedItem);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        dequeueThread.join(AWAIT_MILLIS);

        assertNotEquals(WAITING, dequeueThread.getState());
        assertEquals(0, queue.size());
    }

    @Test
    void secondEnqueueIsBlocked() throws InterruptedException {
        var queue = new CustomBlockingQueue<>(1);
        var firstEnqueueThread = startNewThread(() -> {
            try {
                queue.enqueue(new Object());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        firstEnqueueThread.join(AWAIT_MILLIS);

        assertNotEquals(WAITING, firstEnqueueThread.getState());
        assertEquals(1, queue.size());

        var secondEnqueueThread = startNewThread(() -> {
            try {
                queue.enqueue(new Object());
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        secondEnqueueThread.join(AWAIT_MILLIS);

        assertEquals(WAITING, secondEnqueueThread.getState());
        assertEquals(1, queue.size());
    }

    @Test
    void onlyDequeueIsBlocked() throws InterruptedException {
        var queue = new CustomBlockingQueue<>(1);
        var dequeueThread = startNewThread(() -> {
            try {
                queue.dequeue();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        dequeueThread.join(AWAIT_MILLIS);

        assertEquals(WAITING, dequeueThread.getState());
        assertEquals(0, queue.size());
    }

    @Test
    void queueShouldWorkAsFIFO() throws InterruptedException {
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
        var executorService = Executors.newFixedThreadPool(6);
        var queue = new CustomBlockingQueue<Integer>(2);
        var expectedValues = Set.of(0, 1, 2);
        for (int i = 0; i < 3; i++) {
            executorService.submit(() -> {
                try {
                    var dequeuedItem = queue.dequeue();
                    assertTrue(expectedValues.contains(dequeuedItem));
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        for (int i = 0; i < 3; i++) {
            var item = i;
            executorService.submit(() -> {
                try {
                    queue.enqueue(item);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        executorService.shutdown();

        assertTrue(executorService.awaitTermination(AWAIT_MILLIS, MILLISECONDS));
        assertEquals(0, queue.size());

    }
}