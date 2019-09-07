package com.gitlab.tixtix320.kiwi.internal.observable.subject;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
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
            boolean needMore = observer.consume(object, !completed.get());
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
            for (int i = 0; i < objects.length - 1; i++) {
                T object = objects[i];
                boolean needMore = observer.consume(object, !completed.get());
                if (!needMore) {
                    iterator.remove();
                    break;
                }
            }
            T object = objects[objects.length - 1];
            boolean needMore = observer.consume(object, !completed.get());
            if (!needMore) {
                iterator.remove();
                break;
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
            Iterator<T> objectsItr = objects.iterator();
            while (objectsItr.hasNext()) {
                T object = objectsItr.next();
                boolean needMore = observer.consume(object, !completed.get() && objectsItr.hasNext());
                if (!needMore) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    @Override
    protected Subscription subscribe(ConditionalConsumer<? super Result<? extends T>> consumer) {
        Observer<T> observer = new Observer<>(consumer);
        observers.add(observer);
        nextFromBuffer(observer);
        return () -> observers.remove(observer);
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
        Iterator<T> iterator = buffer.iterator();
        while (iterator.hasNext()) {
            T object = iterator.next();
            boolean needMore = observer.consume(object, iterator.hasNext());
            if (!needMore) {
                observers.remove(observer);
                break;
            }
        }
    }
}
