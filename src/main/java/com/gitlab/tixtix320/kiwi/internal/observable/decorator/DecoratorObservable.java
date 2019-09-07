package com.gitlab.tixtix320.kiwi.internal.observable.decorator;

import com.gitlab.tixtix320.kiwi.api.observable.Observable;
import com.gitlab.tixtix320.kiwi.internal.observable.BaseObservable;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class DecoratorObservable<T> extends BaseObservable<T> {

    @Override
    public final void onComplete(Runnable runnable) {
        AtomicInteger completedCount = new AtomicInteger();
        Collection<Observable<?>> observables = observables();
        for (Observable<?> observable : observables) {
            observable.onComplete(() -> {
                if (completedCount.incrementAndGet() == observables.size()) {
                    runnable.run();
                }
            });
        }
    }

    protected abstract Collection<Observable<?>> observables();
}
