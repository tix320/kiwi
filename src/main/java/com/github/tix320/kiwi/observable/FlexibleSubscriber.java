package com.github.tix320.kiwi.observable;

/**
 * @author : Tigran Sargsyan
 * @since : 15.08.2021
 **/
public abstract class FlexibleSubscriber<T> extends AbstractSubscriber<T> {

	@Override
	public void onSubscribe() {
		// No-op
	}

	@Override
	public void onPublish(T item) {
		// No-op
	}

	@Override
	public void onComplete(Completion completion) {
		// No-op
	}
}
