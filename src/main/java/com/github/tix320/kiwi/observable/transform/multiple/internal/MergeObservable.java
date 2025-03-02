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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class MergeObservable<T> extends Observable<T> {

	private static final SourceCompletion ALL_COMPLETED = new SourceCompletion("MERGE_ALL_COMPLETED");

	private final List<Observable<? extends T>> observables;

	public MergeObservable(List<Observable<? extends T>> observables) {
		if (observables.isEmpty()) {
			throw new IllegalArgumentException("Empty observables");
		}
		this.observables = List.copyOf(observables);
	}

	@Override
	public void subscribe(Subscriber<? super T> subscriber) {
		List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

		Subscription generalSubscription = new Subscription() {

			@Override
			protected void onRequest(long count) {
				throw new UnsupportedOperationException("Currently bound request not supported on Merge");
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
		SharedContext sharedContext = new SharedContext(sourcesCount, generalSubscription,
														subscriptions, new AtomicInteger(0));

		List<Subscriber<T>> subscribers = new ArrayList<>(sourcesCount);
		for (int i = 0; i < sourcesCount; i++) {
			Subscriber<T> sub = subscriber.spawn(new SubscriberImpl<>(sharedContext));
			subscribers.add(sub);
		}

		for (int i = 0; i < observables.size(); i++) {
			observables.get(i).subscribe(subscribers.get(i));
		}
	}

	private static final class SubscriberImpl<T> extends MinorSubscriber<T, T> {

		private final SharedContext context;

		private SubscriberImpl(SharedContext context) {
			this.context = context;
		}

		@Override
		public void onSubscribe(Subscription subscription) {
			context.subscriptions.add(subscription);

			if (context.subscriptions.size() == context.sourcesCount) {
				parent().setSubscription(context.generalSubscription);
			}
		}

		@Override
		public void onNext(T item) {
			parent().publish(item);
		}

		@Override
		public void onComplete(Completion completion) {
			if (context.completedCaughtCount.incrementAndGet() == context.sourcesCount) {
				if (completion instanceof UserUnsubscription userUnsubscription) {
					Unsubscription realUnsubscription = userUnsubscription.realUnsubscription();
					parent().complete(realUnsubscription);
				} else {
					parent().complete(ALL_COMPLETED);
				}
			}
		}

	}

	private record SharedContext(int sourcesCount,
								 Subscription generalSubscription,
								 List<Subscription> subscriptions,
								 AtomicInteger completedCaughtCount) {

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
