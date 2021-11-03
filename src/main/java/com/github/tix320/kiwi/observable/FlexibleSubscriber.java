package com.github.tix320.kiwi.observable;

/**
 * @author : Tigran Sargsyan
 * @since : 15.08.2021
 **/
public abstract class FlexibleSubscriber<T> extends Subscriber<T> {

	@Override
	public void onSubscribe(Subscription subscription) {
		// No-op
	}

	@Override
	public void onNext(T item) {
		// No-op
	}

	@Override
	public void onComplete(Completion completion) {
		// No-op
	}
}
