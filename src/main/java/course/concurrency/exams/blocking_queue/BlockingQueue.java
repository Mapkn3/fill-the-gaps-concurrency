package course.concurrency.exams.blocking_queue;

public interface BlockingQueue<T> {
    void enqueue(T value) throws InterruptedException;

    T dequeue() throws InterruptedException;
}
