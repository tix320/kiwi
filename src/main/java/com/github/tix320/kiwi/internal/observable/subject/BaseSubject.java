package com.github.tix320.kiwi.internal.observable.subject;

import com.github.tix320.kiwi.api.observable.ConditionalConsumer;
import com.github.tix320.kiwi.internal.observable.BaseObservable;
import com.github.tix320.kiwi.internal.observable.CompletedException;
import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.Result;
import com.github.tix320.kiwi.api.observable.Subscription;
import com.github.tix320.kiwi.api.observable.subject.Subject;
import com.github.tix320.kiwi.api.util.IDGenerator;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BaseSubject<T> implements Subject<T> {

    protected final AtomicBoolean completed = new AtomicBoolean(false);

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
        }
    }

    @Override
    public final Observable<T> asObservable() {
        return new SubjectObservable();
    }

    protected void checkCompleted() {
        if (completed.get()) {
            throw new CompletedException("Subject is completed, you cannot do any operations.");
        }
    }

    protected abstract Subscription subscribe(ConditionalConsumer<? super Result<? extends T>> consumer);

    private final class SubjectObservable extends BaseObservable<T> {

        public SubjectObservable() {
        }

        @Override
        public Subscription subscribeAndHandle(ConditionalConsumer<? super Result<? extends T>> consumer) {
            return BaseSubject.this.subscribe(consumer);
        }

        @Override
        public void onComplete(Runnable runnable) {
            if (completed.get()) {
                runnable.run();
            } else {
                completedObservers.add(runnable);
            }
        }
    }

    protected final static class Observer<T> {

        private static final IDGenerator GEN = new IDGenerator();

        private final long id;

        private final ConditionalConsumer<? super Result<? extends T>> consumer;

        protected Observer(ConditionalConsumer<? super Result<? extends T>> consumer) {
            this.id = GEN.next();
            this.consumer = consumer;
        }

        public boolean consume(T object, boolean hasNext) {
            return consumer.consume(Result.of(object, hasNext));
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
