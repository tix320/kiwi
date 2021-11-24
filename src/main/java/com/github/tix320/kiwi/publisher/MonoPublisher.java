package com.github.tix320.kiwi.publisher;

import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.signal.CompleteSignal;
import com.github.tix320.kiwi.observable.signal.PublishSignal;
import com.github.tix320.kiwi.observable.signal.Signal;
import com.github.tix320.kiwi.publisher.internal.BasePublisher;
import com.github.tix320.kiwi.publisher.internal.PublisherCursor;
import com.github.tix320.kiwi.publisher.internal.PublisherSubscription;

/**
 * Mono publisher to publish exactly one object, after which the publisher will be closed.
 * The subscribers will receive that object after subscription immediately.
 */
public final class MonoPublisher<T> extends BasePublisher<T> {

	private volatile PublishSignal<T> valueSignal;

	public MonoPublisher() {
	}

	@Override
	public MonoObservable<T> asObservable() {
		Observable<T> observable = super.asObservable();
		return new MonoObservable<>() {
			@Override
			public void subscribe(Subscriber<? super T> subscriber) {
				observable.subscribe(subscriber);
			}
		};
	}

	@Override
	protected BasePublisher<T>.NormalStrategy getNormalStrategy() {
		return new NormalStrategyImpl();
	}

	@Override
	protected BasePublisher<T>.FreezeStrategy getFreezeStrategy() {
		return new FreezeStrategyImpl();
	}

	@Override
	protected PublisherCursor publishIterator() {
		return new PublisherCursor() {

			volatile boolean receivedItem;
			volatile boolean receivedCompleted;

			@Override
			public Signal get() {
				synchronized (lock) {
					if (!receivedItem) {
						return valueSignal;
					}
					else {
						if (!receivedCompleted) {
							return completion;
						}
						else {
							return null;
						}
					}
				}
			}

			@Override
			public void moveForward() {
				if(receivedItem){
					if(!receivedCompleted){
						receivedCompleted = true;
					}
				}else {
					receivedItem = true;
				}
			}
		};
	}

	private final class NormalStrategyImpl extends NormalStrategy {
		@Override
		public void subscribe(Subscriber<? super T> subscriber, PublisherSubscription<T> subscription) {
			synchronized (lock) {
				//if (valueSignal != null) {
				//	subscription.enqueue(valueSignal);
				//}
				//if (isCompleted()) {
				//	subscription.enqueue(completion);
				//}
				//else {
				subscriptions.add(subscription);
				//}
			}

			subscriber.setSubscription(subscription);

			subscription.start();
		}

		@Override
		public void publish(T item) {
			valueSignal = new PublishSignal<>(item);

			for (PublisherSubscription<T> subscription : subscriptions) {
				subscription.doAction();
			}

			MonoPublisher.this.complete(SourceCompletion.DEFAULT);
		}

		@Override
		public void complete(SourceCompletion sourceCompletion) {
			completion = new CompleteSignal(sourceCompletion);
			subscriptions.forEach(PublisherSubscription::doAction);
			subscriptions.clear();
		}
	}

	private final class FreezeStrategyImpl extends FreezeStrategy {

		private final NormalStrategy normalStrategy = new NormalStrategyImpl();

		private volatile T valueDuringFreeze;

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
			valueDuringFreeze = item;
			MonoPublisher.this.complete(SourceCompletion.DEFAULT);
		}

		@Override
		public void complete(SourceCompletion sourceCompletion) {
			completionDuringFreeze = sourceCompletion;
		}

		@Override
		protected void restore() {
			if (valueDuringFreeze != null) {
				normalStrategy.publish(valueDuringFreeze);
			}
			valueDuringFreeze = null;

			if (completionDuringFreeze != null) {
				completion = new CompleteSignal(completionDuringFreeze);
				subscriptions.forEach(PublisherSubscription::doAction);
				subscriptions.clear();
			}
		}
	}
}
