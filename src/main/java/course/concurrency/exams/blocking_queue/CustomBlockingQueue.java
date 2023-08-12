package course.concurrency.exams.blocking_queue;

import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CustomBlockingQueue<T> implements BlockingQueue<T> {
    private final Lock lock;
    private final Condition notEmpty;
    private final Condition notFull;
    private final int capacity;
    private final LinkedList<T> queue;

    public CustomBlockingQueue(int initCapacity) {
        lock = new ReentrantLock();
        notEmpty = lock.newCondition();
        notFull = lock.newCondition();
        capacity = initCapacity;
        queue = new LinkedList<>();
    }

    @Override
    public void enqueue(T value) throws InterruptedException {
        lock.lockInterruptibly();
        try {
            while (queue.size() == capacity) {
                notFull.await();
            }
            queue.addFirst(value);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public T dequeue() throws InterruptedException {
        T result;
        lock.lockInterruptibly();
        try {
            while (queue.isEmpty()) {
                notEmpty.await();
            }
            result = queue.removeLast();
            notFull.signal();
        } finally {
            lock.unlock();
        }
        return result;
    }

    public int size() {
        return queue.size();
    }
}
