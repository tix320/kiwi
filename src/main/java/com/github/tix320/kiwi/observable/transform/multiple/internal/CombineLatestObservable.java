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
import java.util.concurrent.atomic.AtomicInteger;

public final class CombineLatestObservable<T> extends Observable<List<T>> {

	private static final SourceCompletion ALL_COMPLETED = new SourceCompletion("COMBINE_LATEST_ALL_COMPLETED");

	private static final SourceCompletion COMPLETED_BY_ONE_WITH_NO_ITEMS =
		new SourceCompletion("COMPLETED_BY_ONE_WITH_NO_ITEMS");

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
				for (Subscription subscription : subscriptions) {
					subscription.requestUnbounded();
				}
			}

			@Override
			protected void onCancel(Unsubscription unsubscription) {
				UserUnsubscription userUnsubscription = new UserUnsubscription(unsubscription);
				for (Subscription subscription : subscriptions) {
					subscription.cancel(userUnsubscription);
				}
			}
		};

		int sourcesCount = observables.size();
		List<Source<T>> sources = new ArrayList<>(sourcesCount);

		var sharedContext = new SharedContext<>(generalSubscription, sources, subscriptions,
												new AtomicInteger(0));

		List<Subscriber<T>> subscribers = new ArrayList<>(sourcesCount);
		for (int i = 0; i < sourcesCount; i++) {
			Source<T> source = new Source<>();
			Subscriber<T> sub = subscriber.spawn(new SubscriberImpl<>(sharedContext, source));
			sources.add(source);
			subscribers.add(sub);
		}

		for (int i = 0; i < observables.size(); i++) {
			observables.get(i).subscribe(subscribers.get(i));
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
			var compCount = context.completedCaughtCount.incrementAndGet();

			if (completion instanceof SourceCompletion) { // todo: java 21 switch
				if (compCount == context.sourcesCount()) {
					parent().complete(ALL_COMPLETED);
				} else if (itemHolder.isEmpty()) {
					for (Subscription subscription : context.subscriptions) {
						subscription.cancel();
					}
					parent().complete(COMPLETED_BY_ONE_WITH_NO_ITEMS);
				}
			} else {
				if (!(completion instanceof UserUnsubscription userUnsubscription)) {
					throw new IllegalStateException("Unexpected completion from source in combineLatest: "
													+ completion);
				}

				if (compCount == context.sourcesCount()) {
					var realUnsubscription = userUnsubscription.realUnsubscription();
					parent().complete(realUnsubscription);
				}
			}
		}

	}

	private record SharedContext<T>(Subscription generalSubscription,
									List<Source<T>> sources,
									List<Subscription> subscriptions,
									AtomicInteger completedCaughtCount) {

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
