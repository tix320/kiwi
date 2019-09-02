package com.gitlab.tixtix320.kiwi.internal.observable.subject;

import com.gitlab.tixtix320.kiwi.api.observable.ConditionalConsumer;
import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.api.observable.subject.Subject;
import com.gitlab.tixtix320.kiwi.api.util.IDGenerator;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.CompletedException;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BaseSubject<T> implements Subject<T> {

    private final AtomicBoolean completed = new AtomicBoolean(false);

    private final Collection<Runnable> completedObservers;

    protected final Collection<Observer<? super T>> observers;

    BaseSubject() {
        this.completedObservers = new ConcurrentLinkedQueue<>();
        this.observers = new ConcurrentLinkedQueue<>();
    }

    @Override
    public final void complete() {
        boolean changed = completed.compareAndSet(false, true);
        if (changed) {
            completedObservers.forEach(Runnable::run);
            observers.clear();
            completedObservers.clear();
        } else {
            throw new CompletedException("Subject is already completed");
        }
    }

    public final void onComplete(Runnable runnable) {
        if (completed.get()) {
            runnable.run();
        } else {
            this.completedObservers.add(runnable);
        }
    }

    @Override
    public final Observable<T> asObservable() {
        return new SubjectObservable();
    }

    protected final Observer<T> createObserver(ConditionalConsumer<? super T> consumer) {
        AtomicReference<Observer<T>> observerReference = new AtomicReference<>();
        Subscription subscription = () -> observers.remove(observerReference.get());

        observerReference.set(new Observer<>(object -> {
            boolean needMore = consumer.consume(object);

            if (!needMore) {
                subscription.unsubscribe();
            }
            return needMore;
        }));

        return observerReference.get();
    }

    protected void checkCompleted() {
        if (completed.get()) {
            throw new CompletedException("Subject is completed, you cannot do any operations.");
        }
    }

    protected abstract Subscription subscribe(ConditionalConsumer<? super T> consumer);


    protected abstract int getAvailableObjectsCount();

    private final class SubjectObservable extends BaseObservable<T> {

        public SubjectObservable() {
            BaseSubject.this.onComplete(this::complete);
        }

        @Override
        public Subscription subscribeAndHandle(ConditionalConsumer<? super T> consumer) {
            Subscription subscription = BaseSubject.this.subscribe(consumer);
            addSubscription(subscription);
            return subscription;
        }

        @Override
        public int getAvailableObjectsCount() {
            return BaseSubject.this.getAvailableObjectsCount();
        }
    }

    protected final static class Observer<T> {

        private static final IDGenerator GEN = new IDGenerator();

        private final long id;

        private final ConditionalConsumer<T> consumer;

        private Observer(ConditionalConsumer<T> consumer) {
            this.id = GEN.next();
            this.consumer = consumer;
        }

        public boolean consume(T object) {
            return consumer.consume(object);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Observer<?> observer = (Observer<?>) o;
            return id == observer.id;
        }

        @Override
        public int hashCode() {
            return Long.hashCode(id);
        }
    }

}
