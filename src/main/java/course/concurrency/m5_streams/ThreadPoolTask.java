package course.concurrency.m5_streams;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.Executors.defaultThreadFactory;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class ThreadPoolTask {

    // Task #1
    public ThreadPoolExecutor getLifoExecutor() {
        return new ThreadPoolExecutor(1, 1,
                0L, MILLISECONDS,
                new Lifo<>()
        );
    }

    // Task #2
    public ThreadPoolExecutor getRejectExecutor() {
        var nThreads = 8;
        return new ThreadPoolExecutor(nThreads, nThreads,
                0L, MILLISECONDS,
                new SynchronousQueue<>(),
                defaultThreadFactory(),
                new ThreadPoolExecutor.DiscardPolicy());
    }

    public static class Lifo<E> extends LinkedBlockingDeque<E> implements BlockingQueue<E> {
        public boolean add(E e) {
            this.addFirst(e);
            return true;
        }

        public boolean offer(E e) {
            return this.offerFirst(e);
        }

        public void put(E e) throws InterruptedException {
            this.putFirst(e);
        }

        public boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException {
            return this.offerFirst(e, timeout, unit);
        }
    }

}
