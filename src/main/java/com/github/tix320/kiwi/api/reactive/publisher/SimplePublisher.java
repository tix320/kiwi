package com.github.tix320.kiwi.api.reactive.publisher;

import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class SimplePublisher<T> extends BasePublisher<T, Object> {

	@Override
	protected void subscribe(InternalSubscription<T> subscription) {
		State currentState;
		do {
			currentState = this.state.get();
			if (currentState.isCompleted()) {
				subscription.complete();
				return;
			}
		} while (!this.state.compareAndSet(currentState, currentState.addSubscription(subscription)));
	}

	@Override
	public void publish(T object) {
		State currentState = this.state.get();
		checkCompleted(currentState);
		for (InternalSubscription<T> subscription : currentState.getSubscriptions()) {
			subscription.publishItem(object);
		}
	}
}
