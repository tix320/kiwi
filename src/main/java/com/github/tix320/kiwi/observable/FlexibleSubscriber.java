package com.github.tix320.kiwi.observable;

import com.github.tix320.kiwi.observable.signal.SignalManager;

/**
 * @author : Tigran Sargsyan
 * @since : 15.08.2021
 **/
public abstract class FlexibleSubscriber<T> extends Subscriber<T> {

	protected FlexibleSubscriber(SignalManager signalManager) {
		super(signalManager);
	}

	protected FlexibleSubscriber() {
		super(new SignalManager(1));
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		subscription.request(Long.MAX_VALUE);
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
