package com.github.tix320.kiwi.internal.reactive.observable.transform.single;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.github.tix320.kiwi.api.reactive.common.item.Item;
import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

public final class WaitCompleteObservable<T> extends TransformObservable<T> {

	private final Observable<T> observable;

	public WaitCompleteObservable(Observable<T> observable) {
		this.observable = observable;
	}

	@Override
	public Subscription particularSubscribe(ConditionalConsumer<? super Item<? extends T>> consumer,
											ConditionalConsumer<Throwable> errorHandler) {
		Object waitObject = new Object();
		CompletableFuture.runAsync(() -> observable.particularSubscribe(item -> {
			boolean needMore = consumer.consume(item);
			if (!item.hasNext() || !needMore) {
				synchronized (waitObject) {
					waitObject.notifyAll();
				}
			}
			return needMore;
		}, errorHandler));

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
