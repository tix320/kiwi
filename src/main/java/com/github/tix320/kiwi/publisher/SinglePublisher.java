package com.github.tix320.kiwi.publisher;


import java.util.Objects;

import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.signal.CompleteSignal;
import com.github.tix320.kiwi.observable.signal.PublishSignal;
import com.github.tix320.kiwi.observable.signal.Signal;
import com.github.tix320.kiwi.publisher.internal.BasePublisher;
import com.github.tix320.kiwi.publisher.internal.FlowPublisher;
import com.github.tix320.kiwi.publisher.internal.PublisherCursor;
import com.github.tix320.kiwi.publisher.internal.PublisherSubscription;

public final class SinglePublisher<T> extends FlowPublisher<T> {

	//private volatile NextSignal<T> valueSignal;

	public SinglePublisher() {
	}

	public SinglePublisher(T initialValue) {
		publish(initialValue);
	}

	public boolean CASPublish(T expected, T newValue) {
		Objects.requireNonNull(expected);
		Objects.requireNonNull(newValue);

		synchronized (this) {
			PublishSignal<T> valueSignal = valueSignal();
			T lastItem = valueSignal == null ? null : valueSignal.getItem();
			if (Objects.equals(lastItem, expected)) {
				publish(newValue);
				return true;
			}
			else {
				return false;
			}
		}
	}

	public T getValue() {
		PublishSignal<T> valueSignal = valueSignal();
		if (valueSignal == null) {
			return null;
		}
		return valueSignal.getItem();
	}

	@Override
	protected BasePublisher<T>.NormalStrategy getNormalStrategy() {
		return new NormalStrategyImpl();
	}

	@Override
	protected BasePublisher<T>.FreezeStrategy getFreezeStrategy() {
		return new FreezeStrategyImpl();
	}

	private PublishSignal<T> valueSignal() {
		synchronized (lock) {
			if (queue.isEmpty()) {
				return null;
			}
			else {
				return queue.get(queue.size() - 1);
			}
		}
	}

	private void setValueSignal(PublishSignal<T> valueSignal) {
		synchronized (lock) {
			queue.add(valueSignal);
		}
	}

	@Override
	protected int initialCursor() {
		return Math.max(0, queue.size() - 1);
	}

	private final class NormalStrategyImpl extends NormalStrategy {
		@Override
		public void subscribe(Subscriber<? super T> subscriber, PublisherSubscription<T> subscription) {
			synchronized (lock) {
				//NextSignal<T> valueSignal = valueSignal();
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
			PublishSignal<T> publishSignal = new PublishSignal<>(item);
			setValueSignal(publishSignal);

			for (PublisherSubscription<T> subscription : subscriptions) {
				subscription.doAction();
			}
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
