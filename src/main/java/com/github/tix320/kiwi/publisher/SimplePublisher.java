package com.github.tix320.kiwi.publisher;

import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.signal.CompleteSignal;
import com.github.tix320.kiwi.observable.signal.NextSignal;
import com.github.tix320.kiwi.publisher.internal.BasePublisher;
import com.github.tix320.kiwi.publisher.internal.PublisherSubscription;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class SimplePublisher<T> extends BasePublisher<T> {

	public SimplePublisher() {
		super();
	}

	@Override
	protected BasePublisher<T>.NormalStrategy getNormalStrategy() {
		return new NormalStrategy() {
			@Override
			public void subscribe(Subscriber<? super T> subscriber, PublisherSubscription<T> subscription) {
				synchronized (lock) {
					if (isCompleted()) {
						subscription.enqueue(completion);
					}
					else {
						subscriptions.add(subscription);
					}
				}

				subscriber.setSubscription(subscription);

				subscription.start();
			}

			@Override
			public void publish(T item) {
				NextSignal<T> nextSignal = new NextSignal<>(item);
				for (PublisherSubscription<T> subscription : subscriptions) {
					subscription.next(nextSignal);
				}
			}

			@Override
			public void complete(SourceCompletion sourceCompletion) {
				completion = new CompleteSignal(sourceCompletion);
				subscriptions.forEach(subscription -> subscription.complete(completion));
				subscriptions.clear();
			}
		};
	}

	@Override
	protected BasePublisher<T>.FreezeStrategy getFreezeStrategy() {
		return new FreezeStrategy() {
			@Override
			public void subscribe(Subscriber<? super T> subscriber, PublisherSubscription<T> subscription) {
				synchronized (lock) {
					subscriptions.add(subscription);
				}

				subscriber.setSubscription(subscription);

				subscription.start();
			}

			@Override
			public void publish(T item) {
				// No-op
			}

			@Override
			public void complete(SourceCompletion sourceCompletion) {
				completionDuringFreeze = sourceCompletion;
			}

			@Override
			protected void restore() {
				if (completionDuringFreeze != null) {
					completion = new CompleteSignal(completionDuringFreeze);
					subscriptions.forEach(subscription -> subscription.complete(completion));
					subscriptions.clear();
				}
			}
		};
	}
}
