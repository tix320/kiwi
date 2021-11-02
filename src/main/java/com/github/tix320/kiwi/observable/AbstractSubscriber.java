package com.github.tix320.kiwi.observable;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author : Tigran Sargsyan
 * @since : 15.08.2021
 **/
public abstract class AbstractSubscriber<T> implements Subscriber<T> {

	private final AtomicReference<Subscription> subscription = new AtomicReference<>();

	public final void onSubscribe(Subscription subscription) {
		this.subscription.set(subscription);
		onSubscribe();
	}

	public abstract void onSubscribe();

	public final Subscription subscription() {
		return subscription.get();
	}
}
