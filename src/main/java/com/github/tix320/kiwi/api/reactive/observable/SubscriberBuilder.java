package com.github.tix320.kiwi.api.reactive.observable;

import java.util.Objects;
import java.util.function.Consumer;

import com.github.tix320.kiwi.internal.reactive.observable.StaticSubscriber;

/**
 * @author Tigran Sargsyan on 23-Mar-20.
 */
public class SubscriberBuilder<T> {

	private Consumer<Subscription> onSubscribe;

	private ConditionalConsumer<? super T> onPublish;

	private ConditionalConsumer<Throwable> onError;

	private Consumer<CompletionType> onComplete;

	SubscriberBuilder() {
	}

	public SubscriberBuilder<T> onSubscribe(Consumer<Subscription> onSubscribe) {
		Objects.requireNonNull(onSubscribe);
		this.onSubscribe = onSubscribe;
		return this;
	}

	public SubscriberBuilder<T> onPublish(Consumer<? super T> onPublish) {
		Objects.requireNonNull(onPublish);
		this.onPublish = object -> {
			onPublish.accept(object);
			return true;
		};
		return this;
	}

	public SubscriberBuilder<T> onPublishConditional(ConditionalConsumer<? super T> onPublish) {
		Objects.requireNonNull(onPublish);
		this.onPublish = onPublish;
		return this;
	}

	public SubscriberBuilder<T> onError(Consumer<Throwable> onError) {
		Objects.requireNonNull(onError);
		this.onError = object -> {
			onError.accept(object);
			return true;
		};
		return this;
	}

	public SubscriberBuilder<T> onErrorConditional(ConditionalConsumer<Throwable> onError) {
		Objects.requireNonNull(onError);
		this.onError = onError;
		return this;
	}

	public SubscriberBuilder<T> onComplete(Consumer<CompletionType> onComplete) {
		Objects.requireNonNull(onComplete);
		this.onComplete = onComplete;
		return this;
	}

	public Subscriber<T> build() {
		return new StaticSubscriber<>(onSubscribe, onPublish, onError, onComplete);
	}
}
