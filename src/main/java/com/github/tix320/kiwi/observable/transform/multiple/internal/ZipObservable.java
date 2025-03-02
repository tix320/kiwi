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
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public final class ZipObservable<T> extends Observable<List<T>> {

	private static final SourceCompletion ALL_COMPLETED = new SourceCompletion("ZIP_ALL_COMPLETED");

	private static final SourceCompletion COMPLETED_BECAUSE_SOME_SOURCE_ENDED = new SourceCompletion(
		"ZIP_COMPLETED_BECAUSE_SOME_SOURCE_ENDED");

	private static final Unsubscription UNSUBSCRIPTION_BECAUSE_SOME_SOURCE_ENDED = new Unsubscription(
		"BECAUSE_SOME_SOURCE_ENDED");

	private final List<Observable<? extends T>> observables;

	public ZipObservable(List<Observable<? extends T>> observables) {
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
				for (Subscription subscription : subscriptions) {
					subscription.request(count);
				}
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

		var sharedContext = new SharedContext<>(generalSubscription, sources, subscriptions, new AtomicInteger(0));

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
		private final Source<T> source;

		private SubscriberImpl(SharedContext<T> context, Source<T> source) {
			this.context = context;
			this.source = source;
		}

		@Override

		public void onSubscribe(Subscription subscription) {
			context.subscriptions.add(subscription);

			if (context.subscriptions.size() == context.sourcesCount()) {
				parent().setSubscription(context.generalSubscription);
			}
		}

		@Override
		public void onNext(T item) {
			source.items.add(item);
			boolean notAllReady = context.sources.stream().anyMatch(Source::isEmpty);

			if (notAllReady) {
				return;
			}

			boolean someSourceEnded = false;

			List<T> zip = new ArrayList<>(context.sourcesCount());

			for (var source : context.sources) {
				T queueItem = source.items.poll();
				zip.add(queueItem);

				if (source.completeCaught && source.isEmpty()) {
					someSourceEnded = true;
				}
			}

			parent().publish(zip);

			if (someSourceEnded) {
				for (Subscription subscription : context.subscriptions) {
					subscription.cancel(UNSUBSCRIPTION_BECAUSE_SOME_SOURCE_ENDED);
				}
				parent().complete(COMPLETED_BECAUSE_SOME_SOURCE_ENDED);
			}
		}

		@Override
		public void onComplete(Completion completion) {
			var compCount = context.completedCaughtCount.incrementAndGet();
			source.completeCaught = true;

			if (completion instanceof SourceCompletion) { // todo java 21 switch
				if (compCount == context.sourcesCount()) {
					parent().complete(ALL_COMPLETED);
				} else if (source.isEmpty()) {
					for (Subscription subscription : context.subscriptions) {
						subscription.cancel(UNSUBSCRIPTION_BECAUSE_SOME_SOURCE_ENDED);
					}
					parent().complete(COMPLETED_BECAUSE_SOME_SOURCE_ENDED);
				}
			} else if (completion instanceof UserUnsubscription userUnsubscription) {
				if (compCount == context.sourcesCount()) {
					var realUnsubscription = userUnsubscription.realUnsubscription();
					parent().complete(realUnsubscription);
				}
			} else if (completion != UNSUBSCRIPTION_BECAUSE_SOME_SOURCE_ENDED) {
				throw new IllegalStateException("Unexpected completion from source in zip: " + completion);
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

		final ConcurrentLinkedQueue<T> items = new ConcurrentLinkedQueue<>();
		volatile boolean completeCaught = false;

		boolean isEmpty() {
			return items.isEmpty();
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
