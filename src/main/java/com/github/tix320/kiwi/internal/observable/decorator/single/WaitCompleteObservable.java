package com.github.tix320.kiwi.internal.observable.decorator.single;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.github.tix320.kiwi.api.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.observable.Item;
import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.Subscription;
import com.github.tix320.kiwi.internal.observable.decorator.DecoratorObservable;

public final class WaitCompleteObservable<T> extends DecoratorObservable<T> {

	private final Observable<T> observable;

	public WaitCompleteObservable(Observable<T> observable) {
		this.observable = observable;
	}

	@Override
	public Subscription subscribeAndHandle(ConditionalConsumer<? super Item<? extends T>> consumer) {
		Object waitObject = new Object();
		CompletableFuture.runAsync(() -> observable.subscribeAndHandle(item -> {
			boolean needMore = consumer.consume(item);
			if (!item.hasNext() || !needMore) {
				synchronized (waitObject) {
					waitObject.notifyAll();
				}
			}
			return needMore;
		}));

		observable.onComplete(() -> {
			synchronized (waitObject) {
				waitObject.notifyAll();
			}
		});

		synchronized (waitObject) {
			try {
				waitObject.wait();
			}
			catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
		}
		return () -> {
		};
	}

	@Override
	protected Collection<Observable<?>> decoratedObservables() {
		return Collections.singleton(observable);
	}
}