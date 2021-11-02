package com.github.tix320.kiwi.observable;

/**
 * @author : Tigran Sargsyan
 * @since : 15.08.2021
 **/
public abstract class AbstractSubscriber<T> implements Subscriber<T> {

	private volatile Subscription subscription;

	public final void onSubscribe(Subscription subscription) {
		synchronized (this) {
			if (this.subscription != null) {
				throw new IllegalStateException();
			}
			this.subscription = subscription;
		}
		onSubscribe();
	}

	public abstract void onSubscribe();

	public final Subscription subscription() {
		return subscription;
	}
}
