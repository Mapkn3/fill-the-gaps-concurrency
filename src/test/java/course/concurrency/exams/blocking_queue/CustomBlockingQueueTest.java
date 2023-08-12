package course.concurrency.exams.blocking_queue;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class CustomBlockingQueueTest {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(1);

    @Test
    void enqueueSuccessful() {
        var queue = new CustomBlockingQueue<>(1);
        assertTimeoutPreemptively(DEFAULT_TIMEOUT, () -> queue.enqueue(new Object()));

        assertEquals(1, queue.size());
    }

    @Test
    void dequeueSuccessful() {
        var queue = new CustomBlockingQueue<>(1);
        var item = new Object();
        assertTimeoutPreemptively(DEFAULT_TIMEOUT, () -> queue.enqueue(item));
        assertEquals(1, queue.size());

        var dequeuedItem = assertTimeoutPreemptively(DEFAULT_TIMEOUT, queue::dequeue);
        assertEquals(item, dequeuedItem);
        assertEquals(0, queue.size());
    }

    @Test
    void secondEnqueueIsBlocked() {
        var queue = new CustomBlockingQueue<>(1);
        assertTimeoutPreemptively(DEFAULT_TIMEOUT, () -> queue.enqueue(new Object()));
        var error = assertThrows(
                AssertionFailedError.class,
                () -> assertTimeoutPreemptively(DEFAULT_TIMEOUT, () -> queue.enqueue(new Object()))
        );

        assertEquals(1, queue.size());
        assertEquals("execution timed out after 1000 ms", error.getMessage());
    }

    @Test
    void onlyDequeueIsBlocked() {
        var queue = new CustomBlockingQueue<>(1);
        var error = assertThrows(
                AssertionFailedError.class,
                () -> assertTimeoutPreemptively(DEFAULT_TIMEOUT, queue::dequeue)
        );

        assertEquals(0, queue.size());
        assertEquals("execution timed out after 1000 ms", error.getMessage());
    }
}