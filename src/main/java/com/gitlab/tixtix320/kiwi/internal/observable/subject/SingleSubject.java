package com.gitlab.tixtix320.kiwi.internal.observable.subject;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Result;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;

import java.util.Iterator;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class SingleSubject<T> extends BaseSubject<T> {

    public void next(T object) {
        checkCompleted();
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
        Iterator<Observer<? super T>> iterator = observers.iterator();
        while (iterator.hasNext()) {
            Observer<? super T> observer = iterator.next();
            for (T object : objects) {
                boolean needMore = observer.consume(object, !completed.get());
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
        Iterator<Observer<? super T>> iterator = observers.iterator();
        while (iterator.hasNext()) {
            Observer<? super T> observer = iterator.next();
            for (T object : objects) {
                boolean needMore = observer.consume(object, !completed.get());
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
        return () -> observers.remove(observer);
    }
}
