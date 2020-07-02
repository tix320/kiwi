package com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator;

import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class UntilObservable<T> implements Observable<T> {

	private final Observable<T> observable;

	private final Observable<?> until;

	public UntilObservable(Observable<T> observable, Observable<?> until) {
		this.observable = observable;
		this.until = until;
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		AtomicReference<State> state = new AtomicReference<>(new State(null, false));

		until.subscribe(Subscriber.builder().onComplete((completionType) -> {
			State currentState;
			do {
				currentState = state.get();

			} while (!state.compareAndSet(currentState, new State(currentState.getSubscription(), true)));

			Subscription subscription = currentState.getSubscription();
			if (subscription != null) {
				subscription.unsubscribe();
			}
		}));
		observable.subscribe(new Subscriber<T>() {

			@Override
			public boolean onSubscribe(Subscription subscription) {
				boolean needRegister = subscriber.onSubscribe(
						subscription); // TODO in case of user unsubscription, until subscription not deleted

				if (!needRegister) {
					return false;
				}

				State currentState;
				do {
					currentState = state.get();

					if (currentState.isUntilCompleted()) {
						return false;
					}

				} while (!state.compareAndSet(currentState, new State(subscription, false)));

				return true;
			}

			@Override
			public boolean onPublish(T item) {
				if (state.get().isUntilCompleted()) {
					return false;
				}

				return subscriber.onPublish(item);
			}

			@Override
			public void onComplete(CompletionType completionType) {
				if (state.get().isUntilCompleted()) {
					subscriber.onComplete(CompletionType.SOURCE_COMPLETED);
				}
				else {
					subscriber.onComplete(completionType);
				}
			}
		});
	}

	private static final class State {
		private final Subscription subscription;
		private final boolean untilCompleted;

		public State(Subscription subscription, boolean untilCompleted) {
			this.subscription = subscription;
			this.untilCompleted = untilCompleted;
		}

		public Subscription getSubscription() {
			return subscription;
		}

		public boolean isUntilCompleted() {
			return untilCompleted;
		}
	}
}
