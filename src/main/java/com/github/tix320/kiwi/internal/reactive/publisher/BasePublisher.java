package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.IDGenerator;
import com.github.tix320.kiwi.internal.reactive.CompletedException;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BasePublisher<T> implements Publisher<T> {

	private final IDGenerator subscriberIdGenerator;

	protected final AtomicBoolean completed;

	protected final CopyOnWriteArrayList<InternalSubscription> subscriptions;

	protected final ImprovedReentrantLock publishLock;

	protected BasePublisher() {
		this.subscriberIdGenerator = new IDGenerator(1);
		this.completed = new AtomicBoolean(false);
		this.subscriptions = new CopyOnWriteArrayList<>();
		this.publishLock = new ImprovedReentrantLock();
	}

	@Override
	public final void publish(T object) {
		publishObject(object, true);
	}

	@Override
	public final void publishError(Throwable throwable) {
		publishObject(throwable, false);
	}

	@Override
	public final void complete() {
		completeInternal();
	}

	@Override
	public final boolean isCompleted() {
		return completed.get();
	}

	@Override
	public Observable<T> asObservable() {
		return new PublisherObservable();
	}

	protected abstract boolean onNewSubscriber(InternalSubscription subscription);

	protected void prePublish(Object object, boolean isNormal) {

	}

	protected void publishObject(Object object, boolean isNormal) {
		/* this iterator is based on array snapshot, due the CopyOnWriteArrayList implementation
			 and why we are creating it outside the lock? because Publisher specification requires to publish on that subscribers, which exists in moment of publish
			therefore in case to wait until lock free, array may be changed
			*/
		Iterator<InternalSubscription> subscriptionIterator = subscriptions.iterator();

		publishLock.lock();
		try {
			checkCompleted();

			prePublish(object, isNormal);
		}
		finally {
			publishLock.unlock();
		}

		while (subscriptionIterator.hasNext()) {
			InternalSubscription subscription = subscriptionIterator.next();
			try {
				publishObjectToOneSubscriber(subscription, object, isNormal);
			}
			catch (Exception e) {
				new SubscriberException("An error while publishing to subscriber", e).printStackTrace();
			}
		}
	}

	protected void publishObjectToOneSubscriber(InternalSubscription subscription, Object object, boolean isNormal) {
		if (subscription.isCompleted()) {
			return;
		}
		boolean needMore = isNormal ? subscription.onPublish((T) object) : subscription.onError((Throwable) object);
		if (!needMore) {
			subscription.cancel(CompletionType.UNSUBSCRIPTION);
		}
	}

	protected void completeInternal() {
		boolean changed = completed.compareAndSet(false, true);
		if (changed) {
			for (InternalSubscription subscription : subscriptions) {
				subscription.cancel(CompletionType.SOURCE_COMPLETED);
			}
		}
	}

	protected void checkCompleted() {
		if (completed.get()) {
			throw new CompletedException("Publisher is completed, you can not publish items.");
		}
	}

	public final class PublisherObservable implements Observable<T> {

		@Override
		public void subscribe(Subscriber<? super T> subscriber) {
			InternalSubscription subscription;
			subscription = new InternalSubscription(subscriber);
			boolean needRegister = subscription.onSubscribe(subscription);
			if (!needRegister) {
				subscription.onComplete(CompletionType.UNSUBSCRIPTION);
				return;
			}

			boolean needMore = BasePublisher.this.onNewSubscriber(subscription);

			subscriptions.add(subscription);

			if (!needMore) {
				subscription.cancel(CompletionType.UNSUBSCRIPTION);
			}
			else if (completed.get()) {
				subscription.cancel(CompletionType.SOURCE_COMPLETED);
			}
		}
	}

	protected final class InternalSubscription implements Subscriber<T>, Subscription {

		private final long id;
		private final Subscriber<? super T> subscriber;

		private final AtomicBoolean onSubscribeCalled;
		private final AtomicBoolean onCompleteCalled;

		private InternalSubscription(Subscriber<? super T> subscriber) {
			this.subscriber = subscriber;
			this.id = subscriberIdGenerator.next();
			this.onSubscribeCalled = new AtomicBoolean(false);
			this.onCompleteCalled = new AtomicBoolean(false);
		}

		@Override
		public boolean onSubscribe(Subscription subscription) {
			if (!onSubscribeCalled.compareAndSet(false, true)) {
				throw new SubscriptionIllegalStateException("OnSubscribe must be called only once");
			}

			return subscriber.onSubscribe(subscription);
		}

		@Override
		public boolean onPublish(T item) {
			if (!onSubscribeCalled.get()) {
				throw new SubscriptionIllegalStateException("OnPublish must be called only after onSubscribe");
			}
			if (onCompleteCalled.get()) {
				throw new SubscriptionIllegalStateException("OnPublish must not be called after onComplete");
			}

			return subscriber.onPublish(item);
		}

		@Override
		public boolean onError(Throwable throwable) {
			if (!onSubscribeCalled.get()) {
				throw new SubscriptionIllegalStateException("OnError must be called only after onSubscribe");
			}
			if (onCompleteCalled.get()) {
				throw new SubscriptionIllegalStateException("OnError must not be called after onComplete");
			}

			return subscriber.onError(throwable);
		}

		@Override
		public void onComplete(CompletionType completionType) {
			if (!onSubscribeCalled.get()) {
				throw new SubscriptionIllegalStateException("OnComplete must be called only after onSubscribe");
			}

			if (!onCompleteCalled.compareAndSet(false, true)) {
				throw new SubscriptionIllegalStateException("OnComplete must be called only once");
			}

			subscriber.onComplete(completionType);
		}

		@Override
		public boolean isCompleted() {
			return onCompleteCalled.get();
		}

		public void cancel(CompletionType completionType) {
			boolean removed = subscriptions.remove(this);
			if (removed) {
				onComplete(completionType);
			}
		}

		@Override
		public void unsubscribe() {
			cancel(CompletionType.UNSUBSCRIPTION);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			@SuppressWarnings("unchecked")
			InternalSubscription that = (InternalSubscription) o;
			return id == that.id;
		}

		@Override
		public int hashCode() {
			return Objects.hash(id);
		}
	}
}
