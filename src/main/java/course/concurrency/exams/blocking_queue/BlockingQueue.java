package course.concurrency.exams.blocking_queue;

public interface BlockingQueue<T> {
    void enqueue(T value);

    T dequeue();
}
