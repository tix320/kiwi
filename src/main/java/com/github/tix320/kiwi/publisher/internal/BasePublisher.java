package com.github.tix320.kiwi.publisher.internal;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.signal.CompleteSignal;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.publisher.PublisherCompletedException;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BasePublisher<T> extends Publisher<T> {

	protected final List<PublisherSubscription<T>> subscriptions;

	private final NormalStrategy NORMAL_STRATEGY;
	private final FreezeStrategy FREEZE_STRATEGY;
	private volatile ManageStrategy<T> manageStrategy;

	protected volatile CompleteSignal completion;

	protected final Object lock = new Object();

	protected BasePublisher() {
		this.subscriptions = new CopyOnWriteArrayList<>();
		NORMAL_STRATEGY = getNormalStrategy();
		FREEZE_STRATEGY = getFreezeStrategy();
		this.manageStrategy = NORMAL_STRATEGY;
		this.completion = null;
	}

	@Override
	public final void publish(T object) {
		synchronized (lock) {
			if (isCompleted()) {
				throw new PublisherCompletedException("Publisher is completed, you can not publish items.");
			}

			manageStrategy.publish(object);
		}
	}

	@Override
	public final void complete(SourceCompletion sourceCompletion) {
		synchronized (lock) {
			if (isCompleted()) {
				return;
			}

			manageStrategy.complete(sourceCompletion);
		}
	}

	@Override
	public final boolean isCompleted() {
		return completion != null;
	}

	@Override
	public Observable<T> asObservable() {
		return new PublisherObservable();
	}

	public final void freeze() {
		synchronized (lock) {
			if (isCompleted()) {
				throw new PublisherCompletedException("Publisher completed, freeze not allowed");
			}
			if (!(manageStrategy instanceof FreezeStrategy)) {
				manageStrategy = FREEZE_STRATEGY;
			}
		}
	}

	public final void unfreeze() {
		synchronized (lock) {
			if (manageStrategy instanceof FreezeStrategy freezeStrategy) {
				freezeStrategy.restore();
				manageStrategy = NORMAL_STRATEGY;
			}
		}
	}

	protected abstract NormalStrategy getNormalStrategy();

	protected abstract FreezeStrategy getFreezeStrategy();

	public final boolean isFrozen() {
		return manageStrategy instanceof FreezeStrategy;
	}

	void removeSubscription(PublisherSubscription<T> subscription) {
		synchronized (lock) {
			this.subscriptions.remove(subscription);
		}
	}

	private final class PublisherObservable extends Observable<T> {

		@Override
		public void subscribe(Subscriber<? super T> subscriber) {
			PublisherSubscription<T> subscription = new PublisherSubscription<>(BasePublisher.this, subscriber);

			manageStrategy.subscribe(subscriber, subscription);
		}
	}

	protected interface ManageStrategy<T> {

		void subscribe(Subscriber<? super T> subscriber, PublisherSubscription<T> subscription);

		/**
		 * Called inside a lock.
		 */
		void publish(T item);

		/**
		 * Called inside a lock.
		 */
		void complete(SourceCompletion sourceCompletion);
	}

	protected abstract class NormalStrategy implements ManageStrategy<T> {

		@Override
		public abstract void subscribe(Subscriber<? super T> subscriber, PublisherSubscription<T> subscription);

		@Override
		public abstract void publish(T item);

		@Override
		public abstract void complete(SourceCompletion sourceCompletion);
	}

	protected abstract class FreezeStrategy implements ManageStrategy<T> {

		protected volatile SourceCompletion completionDuringFreeze;

		/**
		 * Called inside a lock.
		 */
		protected abstract void restore();
	}
}
