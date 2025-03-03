package com.github.tix320.kiwi.publisher.internal;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import com.github.tix320.kiwi.observable.demand.DemandStrategy;
import com.github.tix320.kiwi.observable.demand.EmptyDemandStrategy;
import com.github.tix320.kiwi.observable.demand.InfiniteDemandStrategy;
import com.github.tix320.kiwi.observable.signal.CancelSignal;
import com.github.tix320.kiwi.observable.signal.CompleteSignal;
import com.github.tix320.kiwi.observable.signal.ErrorSignal;
import com.github.tix320.kiwi.observable.signal.Signal;
import com.github.tix320.kiwi.observable.signal.SignalSynchronizer;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.publisher.PublisherClosedException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BasePublisher<T> extends Publisher<T> {

	private final List<PublisherSubscription> subscriptions;
	protected final Object lock;

	private volatile CompleteSignal completion;
	private volatile ErrorSignal abortion;

	protected BasePublisher() {
		this.subscriptions = new CopyOnWriteArrayList<>();
		this.lock = new Object();
		this.completion = null;
	}

	@Override
	public final void publish(T object) {
		synchronized (lock) {
			if (isClosed()) {
				throw new PublisherClosedException("Publisher is already closed, items publishing is prohibited");
			}

			onPublish(object);

			subscriptions.forEach(PublisherSubscription::doAction);

		}
	}

	@Override
	public final void complete(SourceCompletion sourceCompletion) {
		synchronized (lock) {
			if (isClosed()) {
				throw new PublisherClosedException("Publisher is already closed, completion is prohibited");
			}

			completion = new CompleteSignal(sourceCompletion);

			subscriptions.forEach(PublisherSubscription::doAction);
			subscriptions.clear();
		}
	}

	@Override
	public final void abort(Throwable throwable) {
		synchronized (lock) {
			if (isClosed()) {
				throw new PublisherClosedException("Publisher is already closed, abortion is prohibited");
			}

			abortion = new ErrorSignal(throwable);

			subscriptions.forEach(PublisherSubscription::doAction);
			subscriptions.clear();
		}
	}

	@Override
	public final boolean isClosed() {
		return completion != null || abortion != null;
	}

	@Override
	public Observable<T> asObservable() {
		return new PublisherObservable();
	}

	/**
	 * Called inside a lock.
	 */
	protected abstract void onPublish(T item);

	protected abstract PublisherCursor createCursor();

	private final class PublisherObservable extends Observable<T> {

		@Override
		public void subscribe(Subscriber<? super T> subscriber) {
			var subscription = new PublisherSubscription(subscriber);

			subscriber.setSubscription(subscription);

			synchronized (lock) {
				if (!isClosed()) {
					subscriptions.add(subscription);
				}
			}

			subscription.activate();
		}

	}

	private final class PublisherSubscription extends Subscription {

		private final SignalSynchronizer.Token token;

		private final PublisherCursor publisherCursor;

		private final AtomicBoolean ended = new AtomicBoolean(false);
		private final AtomicReference<DemandStrategy> demandStrategy =
			new AtomicReference<>(EmptyDemandStrategy.INSTANCE);

		private static final int IDLE = 0;
		private static final int ACQUIRED = 1;
		private static final int SCHEDULED = 2;
		private final AtomicInteger state = new AtomicInteger(IDLE);

		public PublisherSubscription(Subscriber<? super T> realSubscriber) {
			this.token = realSubscriber.createToken();
			this.publisherCursor = createCursor();
		}

		public void activate() {
			token.activate();
		}

		public void doAction() {
			if (ended.get()) {
				return;
			}

			int newValue = state.updateAndGet(value -> switch (value) {
				case IDLE -> ACQUIRED;
				default -> SCHEDULED;
			});

			if (newValue == ACQUIRED) {
				try {
					doActionInternal();
				} finally {
					var prevValue = state.getAndSet(IDLE);
					if (prevValue == SCHEDULED) {
						doAction();
					}
				}
			}

		}

		private void doActionInternal() {
			var demandStrategy = this.demandStrategy.get();
			while (true) {
				var hasPublish = publisherCursor.hasNext();
				var demands = demandStrategy.needMore();

				if (abortion != null) {
					token.addSignal(abortion);
					subscriptions.remove(this);
					ended.set(true);
					break;
				} else if (completion != null && !hasPublish) {
					token.addSignal(completion);
					subscriptions.remove(this);
					ended.set(true);
					break;
				} else if (hasPublish && demands) {
					Signal signal = publisherCursor.next();
					demandStrategy.decrement();
					token.addSignal(signal);
				} else {
					break;
				}
			}
		}

		@Override
		protected void onRequest(long count) {
			demandStrategy.getAndUpdate(strategy -> strategy.addBound(count));
			doAction();
		}

		@Override
		protected void onUnboundRequest() {
			demandStrategy.getAndSet(InfiniteDemandStrategy.INSTANCE);
			doAction();
		}

		@Override
		protected void onCancel(Unsubscription unsubscription) {
			CancelSignal cancelSignal = new CancelSignal(unsubscription);

			token.addSignal(cancelSignal);
		}

	}

}
