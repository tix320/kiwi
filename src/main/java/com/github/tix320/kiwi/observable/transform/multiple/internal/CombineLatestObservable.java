package com.github.tix320.kiwi.observable.transform.multiple.internal;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import com.github.tix320.kiwi.observable.*;

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

		List<Subscription> subscriptions = new CopyOnWriteArrayList<>();
		@SuppressWarnings("unchecked")
		T[] lastItems = (T[]) new Object[observablesCount];

		Completion completion = new Completion(completionType -> {
			synchronized (subscriber) {
				for (Subscription subscription : subscriptions) {
					subscription.unsubscribe();
				}
				subscriber.onComplete(completionType);
			}
		});

		Subscription subscription = new Subscription() {
			@Override
			public boolean isCompleted() {
				synchronized (subscriber) {
					return completion.isFullCompleted();
				}
			}

			@Override
			public void unsubscribe() {
				synchronized (subscriber) {
					completion.fullComplete(CompletionType.UNSUBSCRIPTION);
				}
			}
		};

		boolean needRegister = subscriber.onSubscribe(subscription);
		if (!needRegister) {
			subscriber.onComplete(CompletionType.UNSUBSCRIPTION);
			return;
		}

		for (int i = 0; i < observables.size(); i++) {
			Observable<? extends T> observable = observables.get(i);
			int index = i;
			observable.subscribe(new Subscriber<T>() {

				@Override
				public boolean onSubscribe(Subscription subscription) {
					synchronized (subscriber) {
						if (completion.isFullCompleted()) {
							return false;
						} else {
							subscriptions.add(subscription);
							return true;
						}
					}
				}

				@Override
				public boolean onPublish(T item) {
					Objects.requireNonNull(item,
							"Null values not allowed in " + CombineLatestObservable.class.getSimpleName());

					synchronized (subscriber) {
						lastItems[index] = item;

						for (Object lastItem : lastItems) {
							if (lastItem == null) {
								return true;
							}
						}

						List<T> combinedObjects = List.of(lastItems);
						boolean needMore = subscriber.onPublish(combinedObjects);

						if (!needMore) {
							completion.fullComplete(CompletionType.UNSUBSCRIPTION);
							return false;
						}
						return true;
					}
				}

				@Override
				public void onComplete(CompletionType completionType) {
					synchronized (subscriber) {
						if (completionType == CompletionType.SOURCE_COMPLETED) {
							completion.addComplete();
						}
					}
					// If value is UNSUBSCRIPTION, this is because of cleanup has been run, no need action
				}
			});
		}
	}

	private final class Completion {
		private final Consumer<CompletionType> cleanup;

		private int completedCount;

		public Completion(Consumer<CompletionType> cleanup) {
			this.cleanup = cleanup;
			this.completedCount = 0;
		}

		public void fullComplete(CompletionType completionType) {
			if (isFullCompleted()) {
				throw new IllegalStateException();
			}

			this.completedCount = observables.size();
			this.cleanup.accept(completionType);
		}

		public boolean isFullCompleted() {
			return this.completedCount == observables.size();
		}

		public void addComplete() {
			if (isFullCompleted()) {
				throw new IllegalStateException();
			}

			this.completedCount++;

			if (isFullCompleted()) {
				this.cleanup.accept(CompletionType.SOURCE_COMPLETED);
			}
		}
	}
}
