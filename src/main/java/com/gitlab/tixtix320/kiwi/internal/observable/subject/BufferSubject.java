package com.gitlab.tixtix320.kiwi.internal.observable.subject;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;

import java.util.Deque;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class BufferSubject<T> extends BaseSubject<T> {

    private final Deque<T> buffer;

    private final int bufferCapacity;

    public BufferSubject(int bufferCapacity) {
        buffer = new ConcurrentLinkedDeque<>();
        this.bufferCapacity = Math.max(bufferCapacity, 0);
    }

    public void next(T object) {
        checkCompleted();
        fillBuffer(object);
        Iterator<Observer<? super T>> iterator = observers.iterator();
        while (iterator.hasNext()) {
            Observer<? super T> observer = iterator.next();
            boolean needMore = observer.consume(object);
            if (!needMore) {
                iterator.remove();
            }
        }
    }

    @Override
    public void next(T[] objects) {
        checkCompleted();
        fillBuffer(objects);
        Iterator<Observer<? super T>> iterator = observers.iterator();
        while (iterator.hasNext()) {
            Observer<? super T> observer = iterator.next();
            for (T object : objects) {
                boolean needMore = observer.consume(object);
                if (!needMore) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    @Override
    public void next(Iterable<T> objects) {
        checkCompleted();
        for (T object : objects) {
            fillBuffer(object);
        }
        Iterator<Observer<? super T>> iterator = observers.iterator();
        while (iterator.hasNext()) {
            Observer<? super T> observer = iterator.next();
            for (T object : objects) {
                boolean needMore = observer.consume(object);
                if (!needMore) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    @Override
    protected Subscription subscribe(ConditionalConsumer<? super T> consumer) {
        Observer<T> observer = createObserver(consumer);
        observers.add(observer);
        nextFromBuffer(observer);
        return () -> observers.remove(observer);
    }

    @Override
    protected int getAvailableObjectsCount() {
        return buffer.size();
    }


    private void fillBuffer(T object) {
        if (buffer.size() == bufferCapacity) {
            buffer.removeFirst();
        }
        buffer.addLast(object);
    }


    private void fillBuffer(T[] objects) {
        int removeCount = Math.min(objects.length, bufferCapacity) - (bufferCapacity - buffer.size());
        for (int i = 0; i < removeCount; i++) {
            buffer.removeFirst();
        }
        int insertCount = Math.min(objects.length, bufferCapacity);
        for (int i = objects.length - insertCount; i < objects.length; i++) {
            buffer.addLast(objects[i]);
        }
    }

    private void nextFromBuffer(Observer<? super T> observer) {
        for (T object : buffer) {
            boolean needMore = observer.consume(object);
            if (!needMore) {
                observers.remove(observer);
                break;
            }
        }
    }
}
