package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Objects;
import java.util.Optional;

import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * Mono publisher to publish exactly one object, after which the publisher will be closed.
 * The subscribers will receive that object after subscription immediately.
 */
public final class MonoPublisher<T> extends BasePublisher<T, T> {

	@Override
	public void publish(T object) {
		Objects.requireNonNull(object);

		State currentState;
		do {
			currentState = this.state.get();
			checkCompleted(currentState);

		} while (!this.state.compareAndSet(currentState, currentState.changeCustomState(object).complete()));

		InternalSubscription<T>[] subscriptions = currentState.getSubscriptions();

		for (InternalSubscription<T> subscription : subscriptions) {
			subscription.publishItem(object);
			subscription.complete();
		}
	}

	@Override
	protected void subscribe(InternalSubscription<T> subscription) {
		State currentState;
		do {
			currentState = this.state.get();

			T data = currentState.getCustomState();

			if (data != null) {
				subscription.publishItem(data);
				subscription.complete();
				return;
			}
			else if (currentState.isCompleted()) {
				subscription.complete();
				return;
			}

		} while (!this.state.compareAndSet(currentState, currentState.addSubscription(subscription)));
	}

	@Override
	public MonoObservable<T> asObservable() {
		Observable<T> observable = super.asObservable();
		return observable::subscribe;
	}

	public Optional<T> getContent() {
		return Optional.ofNullable(this.state.get().getCustomState());
	}
}
