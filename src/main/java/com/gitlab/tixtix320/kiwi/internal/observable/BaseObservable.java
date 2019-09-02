package com.gitlab.tixtix320.kiwi.internal.observable;

import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.api.observable.Subscription;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.BlockObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.operator.*;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.reduce.collect.JoinObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.reduce.collect.ToListObservable;
import com.gitlab.tixtix320.kiwi.internal.observable.decorator.single.reduce.collect.ToMapObservable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class BaseObservable<T> implements Observable<T> {

    private final AtomicBoolean completed = new AtomicBoolean(false);

    private final Collection<Runnable> completedObservers;

    private final Collection<Subscription> subscriptions;

    protected BaseObservable() {
        this.completedObservers = new ConcurrentLinkedQueue<>();
        this.subscriptions = new ConcurrentLinkedQueue<>();
    }

    @Override
    public final Subscription subscribe(Consumer<? super T> consumer) {
        return subscribeAndHandle(result -> {
            consumer.accept(result.getValue());
            return true;
        });
    }

    @Override
    public final void complete() {
        boolean changed = completed.compareAndSet(false, true);
        if (changed) {
            completedObservers.forEach(Runnable::run);
            completedObservers.clear();
            subscriptions.forEach(Subscription::unsubscribe);
            subscriptions.clear();
        } else {
            throw new CompletedException("Observable is already completed");
        }
    }

    @Override
    public final void onComplete(Runnable runnable) {
        if (completed.get()) {
            runnable.run();
        } else {
            this.completedObservers.add(runnable);
        }
    }

    protected final void addSubscription(Subscription subscription) {
        subscriptions.add(subscription);
    }

    @Override
    public final Observable<T> block() {
        return new BlockObservable<>(cast(this));
    }

    @Override
    public final Observable<T> take(long count) {
        return new CountingObservable<>(cast(this), count);
    }

    @Override
    public final Observable<T> takeUntil(Observable<?> observable) {
        return new UntilObservable<>(cast(this), observable);
    }

    @Override
    public final Observable<T> one() {
        return new OnceObservable<>(cast(this));
    }

    @Override
    public <R1> Observable<R1> map(Function<? super T, ? extends R1> mapper) {
        return new MapperObservable<>(cast(this), mapper);
    }

    @Override
    public final Observable<T> filter(Predicate<? super T> filter) {
        return new FilterObservable<>(cast(this), filter);
    }

    @Override
    public final <K, V> Observable<Map<K, V>> toMap(Function<? super T, ? extends K> keyMapper,
                                                    Function<? super T, ? extends V> valueMapper) {
        return new ToMapObservable<>(cast(this), keyMapper, valueMapper);
    }

    @Override
    public final Observable<List<T>> toList() {
        return new ToListObservable<>(cast(this));
    }

    @Override
    public final Observable<String> join(Function<? super T, ? extends String> toString, String delimiter) {
        return new JoinObservable<>(cast(this), toString, delimiter);
    }

    @Override
    public final Observable<String> join(Function<? super T, ? extends String> toString, String delimiter,
                                         String prefix, String suffix) {
        return new JoinObservable<>(cast(this), toString, delimiter, prefix, suffix);
    }

    private void checkCompleted() {
        if (completed.get()) {
            throw new CompletedException("Observable is completed");
        }
    }

    @SuppressWarnings("unchecked")
    private <A> BaseObservable<A> cast(Observable<?> observable) {
        if (observable instanceof BaseObservable) {
            return (BaseObservable) observable;
        } else {
            throw new IllegalStateException(String.format("This is not for your implementation %s", observable.getClass()));
        }
    }
}
