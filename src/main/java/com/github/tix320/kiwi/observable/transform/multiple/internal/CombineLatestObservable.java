package com.github.tix320.kiwi.observable.transform.multiple.internal;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.MinorSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public final class CombineLatestObservable<T> extends Observable<List<T>> {

	private static final SourceCompletion ALL_COMPLETED = new SourceCompletion("COMBINE_LATEST_ALL_COMPLETED");

	private static final SourceCompletion COMPLETED_BY_ONE_WITH_NO_ITEMS =
		new SourceCompletion("COMPLETED_BY_ONE_WITH_NO_ITEMS");
	private static final Unsubscription UNSUBSCRIPTION_BY_ONE_WITH_NO_ITEMS =
		new Unsubscription("COMBINE_LATEST_ONE_WITH_NO_ITEMS");

	private final List<Observable<? extends T>> observables;

	public CombineLatestObservable(List<Observable<? extends T>> observables) {
		if (observables.isEmpty()) {
			throw new IllegalArgumentException("Empty observables");
		}
		this.observables = List.copyOf(observables);
	}

	@Override
	public void subscribe(Subscriber<? super List<T>> subscriber) {
		List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

		Subscription generalSubscription = new Subscription() {

			@Override
			protected void onRequest(long count) {
				throw new UnsupportedOperationException("Currently bound request not supported on CombineLatest");
			}

			@Override
			protected void onUnboundRequest() {
				SubscriptionUtils.requestUnboundedAll(subscriptions);
			}

			@Override
			protected void onCancel(Unsubscription unsubscription) {
				UserUnsubscription userUnsubscription = new UserUnsubscription(unsubscription);
				SubscriptionUtils.cancelAll(subscriptions, userUnsubscription);
			}
		};

		int sourcesCount = observables.size();
		List<Source<T>> sources = new ArrayList<>(sourcesCount);

		var sharedContext = new SharedContext<>(generalSubscription, sources, subscriptions,
												new AtomicInteger(0), new AtomicBoolean(false));

		List<Subscriber<T>> subscribers = new ArrayList<>(sourcesCount);
		for (int i = 0; i < sourcesCount; i++) {
			Source<T> source = new Source<>();
			Subscriber<T> sub = subscriber.spawn(new SubscriberImpl<>(sharedContext, source));
			sources.add(source);
			subscribers.add(sub);
		}

		try {
			for (int i = 0; i < observables.size(); i++) {
				observables.get(i).subscribe(subscribers.get(i));
			}
		} catch (Exception exception) {
			SubscriptionUtils.rollback(subscriptions, exception);
		}
	}

	private static final class SubscriberImpl<T> extends MinorSubscriber<T, List<T>> {

		private final SharedContext<T> context;
		private final Source<T> itemHolder;

		private SubscriberImpl(SharedContext<T> context, Source<T> itemHolder) {
			super();
			this.context = context;
			this.itemHolder = itemHolder;
		}

		@Override
		public void onSubscribe(Subscription subscription) {
			var subscriptions = context.subscriptions;
			subscriptions.add(subscription);

			if (subscriptions.size() == context.sourcesCount()) {
				parent().setSubscription(context.generalSubscription);
			}
		}

		@Override
		public void onNext(T item) {
			if (context.terminated.get()) {
				return;
			}
			itemHolder.item = item;

			List<T> combined = context.sources.stream()
				.map(Source::item)
				.filter(Objects::nonNull)
				.toList();

			if (combined.size() != context.sourcesCount()) {
				return;
			}

			parent().publish(combined);
		}

		@Override
		public void onComplete(Completion completion) {
			if (context.terminated.get()) {
				return;
			}
			var compCount = context.completedCaughtCount.incrementAndGet();

			switch (completion) {
				case SourceCompletion ignored -> {
					if (compCount == context.sourcesCount() && context.terminated.compareAndSet(false, true)) {
						parent().complete(ALL_COMPLETED);
					} else if (itemHolder.isEmpty() && context.terminated.compareAndSet(false, true)) {
						SubscriptionUtils.cancelAllThen(context.subscriptions,
							UNSUBSCRIPTION_BY_ONE_WITH_NO_ITEMS,
							() -> parent().complete(COMPLETED_BY_ONE_WITH_NO_ITEMS));
					}
				}
				case UserUnsubscription userUnsubscription -> {
					if (compCount == context.sourcesCount() && context.terminated.compareAndSet(false, true)) {
						parent().complete(userUnsubscription.realUnsubscription());
					}
				}
				case Unsubscription ignored -> {
					if (completion != UNSUBSCRIPTION_BY_ONE_WITH_NO_ITEMS) {
						throw new IllegalStateException(
							"Unexpected completion from source in combineLatest: " + completion);
					}
				}
			}
		}

		@Override
		protected void onError(Throwable error) {
			if (context.terminated.compareAndSet(false, true)) {
				SubscriptionUtils.failAll(context.subscriptions, parent(), error);
			}
		}

	}

	private record SharedContext<T>(Subscription generalSubscription,
									List<Source<T>> sources,
									List<Subscription> subscriptions,
									AtomicInteger completedCaughtCount,
									AtomicBoolean terminated) {

		private int sourcesCount() {
			return sources.size();
		}

	}

	private static final class Source<T> {

		volatile T item;

		T item() {
			return item;
		}

		boolean isEmpty() {
			return item == null;
		}

	}

	private static final class UserUnsubscription extends Unsubscription {

		public UserUnsubscription(Unsubscription data) {
			super(data);
		}

		public Unsubscription realUnsubscription() {
			return data();
		}

	}

}
