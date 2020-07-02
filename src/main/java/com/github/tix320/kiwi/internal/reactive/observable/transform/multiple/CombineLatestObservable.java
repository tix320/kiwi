package com.github.tix320.kiwi.internal.reactive.observable.transform.multiple;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import com.github.tix320.kiwi.api.reactive.observable.*;
import com.github.tix320.kiwi.internal.reactive.publisher.ArrayUtils;

//TODO rair bug, when complete and publish is parralel
public final class CombineLatestObservable<T> implements TransformObservable<T, List<T>> {

	private final List<Observable<? extends T>> observables;

	public CombineLatestObservable(List<Observable<? extends T>> observables) {
		if (observables.size() == 0) {
			throw new IllegalArgumentException();
		}
		this.observables = List.copyOf(observables);
	}

	@Override
	public void subscribe(Subscriber<? super List<T>> subscriber) {
		int observablesCount = observables.size();
		AtomicReference<State> state = new AtomicReference<>(new State(observablesCount));

		Consumer<CompletionType> cleanup = (completionType) -> {
			State currentState;
			do {
				currentState = state.get();

			} while (!state.compareAndSet(currentState, currentState.changeCompleted(true)));

			Subscription[] subscriptions = currentState.getSubscriptions();
			for (Subscription subscription : subscriptions) {
				subscription.unsubscribe();
			}
			subscriber.onComplete(completionType);
		};

		Subscription subscription = new Subscription() {
			@Override
			public boolean isCompleted() {
				return state.get().isCompleted();
			}

			@Override
			public void unsubscribe() {
				cleanup.accept(CompletionType.UNSUBSCRIPTION);
			}
		};
		subscriber.onSubscribe(subscription);

		for (int i = 0; i < observables.size(); i++) {
			Observable<? extends T> observable = observables.get(i);
			int index = i;
			observable.subscribe(new Subscriber<T>() {

				@Override
				public boolean onSubscribe(Subscription subscription) {
					State currentState;
					State newState;
					do {
						currentState = state.get();
						if (currentState.isCompleted()) {
							return false;
						}

						newState = currentState.addSubscription(index, subscription);
					} while (!state.compareAndSet(currentState, newState));

					return true;
				}

				@Override
				public boolean onPublish(T item) {
					State currentState;
					State newState;
					do {
						currentState = state.get();
						if (currentState.isCompleted()) {
							return false;
						}

						newState = currentState.changeLastItem(index, item);
					} while (!state.compareAndSet(currentState, newState));

					Object[] lastItems = newState.getLastItems();

					for (Object lastItem : lastItems) {
						if (lastItem == null) {
							return true;
						}
					}

					List<T> combinedObjects = new ArrayList<>(lastItems.length);
					for (Object obj : lastItems) {
						@SuppressWarnings("unchecked")
						T casted = (T) obj;
						combinedObjects.add(casted);
					}
					boolean needMore = subscriber.onPublish(combinedObjects);

					if (!needMore) {
						cleanup.accept(CompletionType.UNSUBSCRIPTION);
						return false;
					}
					return true;
				}

				@Override
				public void onComplete(CompletionType completionType) {
					if (completionType == CompletionType.SOURCE_COMPLETED) {
						cleanup.accept(CompletionType.SOURCE_COMPLETED);
					}
				}
			});
		}
	}

	private static final class State {
		private final boolean completed;
		private final Subscription[] subscriptions;
		private final Object[] lastItems;

		private State(boolean completed, Subscription[] subscriptions, Object[] lastItems) {
			this.completed = completed;
			this.subscriptions = subscriptions;
			this.lastItems = lastItems;
		}

		public State(int observablesCount) {
			this(false, new Subscription[observablesCount], new Object[observablesCount]);
		}

		public boolean isCompleted() {
			return completed;
		}

		public Subscription[] getSubscriptions() {
			return subscriptions;
		}

		public Object[] getLastItems() {
			return lastItems;
		}

		public State changeCompleted(boolean completed) {
			return new State(completed, this.subscriptions, this.lastItems);
		}

		public State addSubscription(int index, Subscription subscription) {
			Subscription[] subscriptions = ArrayUtils.changeItem(this.subscriptions, index, subscription,
					Subscription[]::new);
			return new State(this.completed, subscriptions, this.lastItems);
		}

		public State changeLastItem(int index, Object item) {
			Object[] lastItems = ArrayUtils.changeItem(this.lastItems, index, item, Object[]::new);
			return new State(this.completed, this.subscriptions, lastItems);
		}
	}
}
