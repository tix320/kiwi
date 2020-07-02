package com.github.tix320.kiwi.internal.reactive.observable;

import java.util.Objects;
import java.util.function.Consumer;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;

/**
 * @author Tigran Sargsyan on 23-Mar-20.
 */
public class StaticSubscriber<T> implements Subscriber<T> {

	private final ConditionalConsumer<Subscription> onSubscribe;

	private final ConditionalConsumer<? super T> onPublish;

	private final Consumer<CompletionType> onComplete;

	public StaticSubscriber(ConditionalConsumer<Subscription> onSubscribe, ConditionalConsumer<? super T> onPublish,
							Consumer<CompletionType> onComplete) {
		this.onSubscribe = Objects.requireNonNullElse(onSubscribe, subscription -> true);
		this.onPublish = Objects.requireNonNullElse(onPublish, subscription -> true);
		this.onComplete = Objects.requireNonNullElse(onComplete, (c) -> {});
	}

	@Override
	public boolean onSubscribe(Subscription subscription) {
		return onSubscribe.accept(subscription);
	}

	@Override
	public boolean onPublish(T item) {
		return onPublish.accept(item);
	}

	@Override
	public void onComplete(CompletionType completionType) {
		onComplete.accept(completionType);
	}
}
