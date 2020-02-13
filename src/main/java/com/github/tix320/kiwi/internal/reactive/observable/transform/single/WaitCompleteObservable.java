package com.github.tix320.kiwi.internal.reactive.observable.transform.single;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;

import com.github.tix320.kiwi.api.check.Try;
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
		CountDownLatch latch = new CountDownLatch(1);
		CompletableFuture.runAsync(() -> {
			observable.particularSubscribe(item -> {
				boolean needMore = consumer.consume(item);
				if (!item.hasNext() || !needMore) {
					latch.countDown();
				}
				return needMore;
			}, errorHandler);

			observable.onComplete(latch::countDown);
		});

		Try.run(latch::await);

		return () -> {
		};
	}

	@Override
	protected Collection<Observable<?>> decoratedObservables() {
		return Collections.singleton(observable);
	}
}
