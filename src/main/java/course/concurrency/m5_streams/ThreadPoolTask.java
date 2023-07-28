package course.concurrency.m5_streams;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyIterator;
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
                new EmptyBlockingQueue<>(),
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

    public static class EmptyBlockingQueue<E> implements BlockingQueue<E> {
        @Override
        public boolean add(E e) {
            return false;
        }

        @Override
        public boolean offer(E e) {
            return false;
        }

        @Override
        public E remove() {
            return null;
        }

        @Override
        public E poll() {
            return null;
        }

        @Override
        public E element() {
            return null;
        }

        @Override
        public E peek() {
            return null;
        }

        @Override
        public void put(E e) {

        }

        @Override
        public boolean offer(E e, long timeout, TimeUnit unit) {
            return false;
        }

        @Override
        public E take() throws InterruptedException {
            throw new InterruptedException();
        }

        @Override
        public E poll(long timeout, TimeUnit unit) {
            return null;
        }

        @Override
        public int remainingCapacity() {
            return 0;
        }

        @Override
        public boolean remove(Object o) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return false;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return false;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return false;
        }

        @Override
        public void clear() {

        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<E> iterator() {
            return emptyIterator();
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return a;
        }

        @Override
        public int drainTo(Collection<? super E> c) {
            return 0;
        }

        @Override
        public int drainTo(Collection<? super E> c, int maxElements) {
            return 0;
        }
    }
}
