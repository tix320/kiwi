package com.github.tix320.kiwi.observable.transform.multiple.internal;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.observable.*;
import com.github.tix320.kiwi.observable.transform.multiple.internal.SignalSerializerObservable.ObservableSignal;
import com.github.tix320.kiwi.publisher.Signal;

/**
 * @author Tigran Sargsyan on 16-May-21
 */
public final class SignalSerializerObservable<T> implements Observable<ObservableSignal<T>> {

	private static final Comparator<Signal> SIGNAL_COMPARATOR = Comparator.comparing(Signal::order);

	private final Map<String, Observable<? extends T>> observables;

	public SignalSerializerObservable(Map<String, Observable<? extends T>> observables) {
		if (observables.size() == 0) {
			throw new IllegalArgumentException();
		}
		this.observables = Map.copyOf(observables);
	}

	@Override
	public void subscribe(Subscriber<? super ObservableSignal<T>> subscriber) {
		int observablesCount = observables.size();

		AtomicInteger subscribedCount = new AtomicInteger(0);
		CopyOnWriteArrayList<OpenSignalSubscription> subscriptions = new CopyOnWriteArrayList<>();

		AtomicReference<Subscription> generalSubscriptionHolder = new AtomicReference<>();

		Object lock = new Object();

		HashMap<String, Signal> lastItems = new HashMap<>(observablesCount);

		for (Entry<String, Observable<? extends T>> entry : observables.entrySet()) {
			final String sourceKey = entry.getKey();
			final Observable<? extends T> observable = entry.getValue();

			observable.subscribe(new Subscriber<T>() {

				@Override
				public void onSubscribe() {
					if (!(subscription() instanceof OpenSignalSubscription)) {
						throw new OpenSignalUnsupportedException(
								String.format("Provided observable does not support %s: %s",
										OpenSignalSubscription.class.getSimpleName(), observable));
					}

					OpenSignalSubscription subscription = (OpenSignalSubscription) subscription();

					subscription.freeze();
					subscriptions.add(subscription);

					if (subscribedCount.incrementAndGet() == observablesCount) {

						Subscription generalSubscription = new OpenSignalSubscription() {
							@Override
							public void freeze() {
								synchronized (lock) {
									subscriptions.forEach(Subscription::freeze);
								}
							}

							@Override
							public void unfreeze() {
								synchronized (lock) {
									subscriptions.forEach(Subscription::unfreeze);
								}
							}

							@Override
							public Optional<Signal> currentSignal() {
								synchronized (lock) {
									return subscriptions.stream()
											.map(OpenSignalSubscription::currentSignal)
											.filter(Optional::isPresent)
											.map(Optional::get)
											.min(SIGNAL_COMPARATOR);
								}
							}

							@Override
							public void unsubscribe(Unsubscription unsubscription) {
								synchronized (lock) {
									for (Subscription subscription : subscriptions) {
										subscription.unsubscribe();
									}
									subscriber.onComplete(unsubscription);
								}
							}

							@Override
							public void unsubscribeImmediately(Unsubscription unsubscription) {
								synchronized (lock) {
									for (Subscription subscription : subscriptions) {
										subscription.unsubscribeImmediately();
									}
									subscriber.onComplete(unsubscription);
								}
							}
						};

						generalSubscriptionHolder.set(generalSubscription);

						subscriber.onSubscribe(generalSubscription);

						subscriptions.forEach(Subscription::unfreeze);

					}
				}

				@Override
				public void onPublish(T item) {
					tryPublishSignal();
				}

				@Override
				public void onComplete(Completion completion) {
					tryPublishSignal();
				}

				private void tryPublishSignal() {
					synchronized (lock) {
						final OpenSignalSubscription currentSubscription = castedSubscription();
						final Signal currentSignal = currentSubscription.currentSignal().orElseThrow();

						Signal minSignal = currentSignal;
						Subscription minSignalSubscription = currentSubscription;

						for (OpenSignalSubscription subscription : subscriptions) {
							final Signal signal = subscription.currentSignal().orElse(null);
							if (signal != null && minSignal.compareTo(signal) < 0) {
								minSignal = signal;
								minSignalSubscription = subscription;
							}

						}

						if (minSignal.equals(currentSignal)) {
							subscriber.onPublish(new ObservableSignal(sourceKey, currentSignal));
							lastItems.entrySet().stream().sorted(Entry.comparingByValue()).forEach(entry -> {
								final String sourceKey = entry.getKey();
								lastItems.remove(sourceKey);
								subscriber.onPublish(new ObservableSignal(sourceKey, entry.getValue()));
							});
						} else {
							lastItems.merge(sourceKey, currentSignal,
									(key, value) -> {throw new IllegalStateException(key + ":" + value);});
							currentSubscription.freeze();
						}

					}
				}

				private OpenSignalSubscription castedSubscription() {
					return (OpenSignalSubscription) subscription();
				}
			});
		}
	}

	public static final class ObservableSignal {
		private final String sourceKey;
		private final Signal signal;

		public ObservableSignal(String sourceKey, Signal signal) {
			this.sourceKey = sourceKey;
			this.signal = signal;
		}

		public String sourceKey() {
			return sourceKey;
		}

		public Signal signal() {
			return signal;
		}
	}
}
