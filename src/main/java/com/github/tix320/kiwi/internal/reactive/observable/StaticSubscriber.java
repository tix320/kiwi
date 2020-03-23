package com.github.tix320.kiwi.internal.reactive.observable;

import java.util.Objects;
import java.util.function.Consumer;

import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;

/**
 * @author Tigran Sargsyan on 23-Mar-20.
 */
public class StaticSubscriber<T> implements Subscriber<T> {

	private final Consumer<Subscription> onSubscribe;

	private final ConditionalConsumer<? super T> onPublish;

	private final ConditionalConsumer<Throwable> onError;

	private final Runnable onComplete;

	public StaticSubscriber(Consumer<Subscription> onSubscribe, ConditionalConsumer<? super T> onPublish,
							ConditionalConsumer<Throwable> onError, Runnable onComplete) {
		this.onSubscribe = Objects.requireNonNullElse(onSubscribe, subscription -> { });
		this.onPublish = Objects.requireNonNullElse(onPublish, subscription -> true);
		this.onError = Objects.requireNonNullElse(onError,
				throwable -> {throw new UnhandledObservableException(throwable);});
		this.onComplete = Objects.requireNonNullElse(onComplete, () -> {});
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		onSubscribe.accept(subscription);
	}

	@Override
	public boolean onPublish(T item) {
		return onPublish.accept(item);
	}

	@Override
	public boolean onError(Throwable throwable) {
		return onError.accept(throwable);
	}

	@Override
	public void onComplete() {
		onComplete.run();
	}
}
