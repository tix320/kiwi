package com.github.tix320.kiwi.observable.transform.multiple.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.observable.CompletionType;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class ConcatObservable<T> implements Observable<T> {

	private final List<Observable<? extends T>> observables;

	public ConcatObservable(List<Observable<? extends T>> observables) {
		if (observables.size() == 0) {
			throw new IllegalArgumentException();
		}
		this.observables = List.copyOf(observables);
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		int observablesCount = observables.size();
		List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

		AtomicReference<State> state = new AtomicReference<>(new State(false, 0));

		Subscription subscription = new Subscription() {
			@Override
			public boolean isCompleted() {
				State currentState = state.get();
				return currentState.isUnsubscribed() || currentState.getCompletedCount() == observablesCount;
			}

			@Override
			public void unsubscribe() {
				State currentState;
				do {
					currentState = state.get();

					if (currentState.isUnsubscribed()) {
						return;
					}

				} while (!state.compareAndSet(currentState, new State(true, currentState.getCompletedCount())));

				for (Subscription subscription : subscriptions) {
					subscription.unsubscribe();
				}
				subscriber.onComplete(CompletionType.UNSUBSCRIPTION);
			}
		};

		subscriber.onSubscribe(subscription);

		Subscriber<? super T> generalSubscriber = new Subscriber<>() {
			@Override
			public boolean onSubscribe(Subscription subscription) {
				State currentState = state.get();
				if (currentState.isUnsubscribed()) {
					return false;
				}
				subscriptions.add(subscription);
				return true;
			}

			@Override
			public boolean onPublish(T item) {
				synchronized (subscriber) {
					if (state.get().isUnsubscribed()) {
						return false;
					}

					boolean needMore = subscriber.onPublish(item);

					if (needMore) {
						return true;
					}
					else {
						state.updateAndGet(s -> new State(true, s.getCompletedCount()));
						return false;
					}
				}

			}

			@Override
			public void onComplete(CompletionType completionType) {
				State currentState;
				State newState;
				do {
					currentState = state.get();
					if (currentState.isUnsubscribed()) {
						return;
					}

					newState = new State(false, currentState.getCompletedCount() + 1);
				} while (!state.compareAndSet(currentState, newState));

				if (newState.getCompletedCount() == observablesCount) {
					subscriber.onComplete(CompletionType.SOURCE_COMPLETED);
				}
			}
		};

		for (Observable<? extends T> observable : observables) {
			observable.subscribe(generalSubscriber);
		}
	}

	private static final class State {
		private final boolean unsubscribed;
		private final int completedCount;

		private State(boolean unsubscribed, int completedCount) {
			this.unsubscribed = unsubscribed;
			this.completedCount = completedCount;
		}

		public boolean isUnsubscribed() {
			return unsubscribed;
		}

		public int getCompletedCount() {
			return completedCount;
		}
	}
}
