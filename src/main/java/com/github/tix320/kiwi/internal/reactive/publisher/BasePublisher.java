package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.github.tix320.kiwi.api.reactive.observable.*;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.IDGenerator;
import com.github.tix320.kiwi.internal.reactive.CompletedException;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BasePublisher<T> implements Publisher<T> {

	private final IDGenerator subscriberIdGenerator;

	private final AtomicBoolean completed;

	private final CopyOnWriteArrayList<InternalSubscription> subscriptions;

	private final Lock publishLock;

	protected BasePublisher() {
		this.subscriberIdGenerator = new IDGenerator(1);
		this.completed = new AtomicBoolean(false);
		this.subscriptions = new CopyOnWriteArrayList<>();
		this.publishLock = new ReentrantLock();
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
		boolean changed = completed.compareAndSet(false, true);
		if (changed) {
			for (InternalSubscription subscription : subscriptions) {
				subscription.cancel(CompletionType.SOURCE_COMPLETED);
			}
		}
	}

	@Override
	public final boolean isCompleted() {
		return completed.get();
	}

	@Override
	public Observable<T> asObservable() {
		return new PublisherObservable();
	}

	protected abstract void onNewSubscriber(ConditionalConsumer<T> publisherFunction);

	protected void prePublish(T object) {

	}

	protected void postPublish() {

	}

	private void publishObject(Object object, boolean isNormal) {
		Iterator<InternalSubscription> subscriptionIterator;
		publishLock.lock();
		try {
			failIfCompleted();
			if (isNormal) {
				prePublish((T) object);
			}
			/* this iterator is based on array snapshot, due the CopyOnWriteArrayList implementation
			 and why we are creating in lock? because Publisher specification requires to publish on that subscribers, which exists in moment of publish
			therefore if creating outside the lock, array may be changed
			*/
			subscriptionIterator = subscriptions.iterator();
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

		publishLock.lock();
		try {

			if (isNormal) {
				postPublish();
			}
		}
		finally {
			publishLock.unlock();
		}
	}

	private void publishObjectToOneSubscriber(InternalSubscription subscription, Object object, boolean isNormal) {
		boolean needMore = isNormal ? subscription.onPublish((T) object) : subscription.onError((Throwable) object);
		if (!needMore) {
			subscription.cancel(CompletionType.UNSUBSCRIPTION);
		}
	}

	private void failIfCompleted() {
		if (completed.get()) {
			throw new CompletedException("Publisher is completed, you can not publish items.");
		}
	}

	public final class PublisherObservable implements Observable<T> {

		@Override
		public void subscribe(Subscriber<? super T> subscriber) {
			InternalSubscription subscription;
			AtomicBoolean needMoreHolder = new AtomicBoolean(true);
			subscription = new InternalSubscription(subscriber);
			boolean needRegister = subscription.onSubscribe(subscription);
			if (!needRegister) {
				subscription.subscriptionLock.lock();
				try {
					subscription.completed = true;
					subscription.onComplete(CompletionType.UNSUBSCRIPTION);
				}
				finally {
					subscription.subscriptionLock.unlock();
				}
				return;
			}
			subscriptions.add(subscription);

			publishLock.lock();
			try {

				BasePublisher.this.onNewSubscriber(object -> {
					boolean needMore = subscription.onPublish(object);
					needMoreHolder.set(needMore);
					return needMore;
				});
			}
			finally {
				publishLock.unlock();
			}

			if (completed.get()) {
				subscription.cancel(CompletionType.SOURCE_COMPLETED);
			}

			else if (!needMoreHolder.get()) {
				subscription.cancel(CompletionType.UNSUBSCRIPTION);
			}
		}
	}

	private final class InternalSubscription implements Subscriber<T>, Subscription {

		private final long id;
		private final Subscriber<? super T> subscriber;

		private final Lock subscriptionLock;

		public volatile boolean completed;

		// for validation, antiBug
		private volatile boolean onSubscribeCalled;
		private volatile boolean onCompleteCalled;

		private InternalSubscription(Subscriber<? super T> subscriber) {
			this.subscriber = subscriber;
			this.id = subscriberIdGenerator.next();
			this.subscriptionLock = new ReentrantLock();
			this.onSubscribeCalled = false;
			this.onCompleteCalled = false;
			this.completed = false;
		}

		@Override
		public boolean onSubscribe(Subscription subscription) {
			subscriptionLock.lock();
			try {
				if (onSubscribeCalled) {
					throw new SubscriptionIllegalStateException("OnSubscribe must be called only once");
				}
				onSubscribeCalled = true;
			}
			finally {
				subscriptionLock.unlock();
			}

			return subscriber.onSubscribe(subscription);
		}

		@Override
		public boolean onPublish(T item) {
			subscriptionLock.lock();
			try {
				if (completed) {
					return false;
				}
				if (!onSubscribeCalled) {
					throw new SubscriptionIllegalStateException("OnPublish must be called only after onSubscribe");
				}
				if (onCompleteCalled) {
					throw new SubscriptionIllegalStateException("OnPublish must not be called after onComplete");
				}
			}
			finally {
				subscriptionLock.unlock();
			}

			return subscriber.onPublish(item);
		}

		@Override
		public boolean onError(Throwable throwable) {
			subscriptionLock.lock();
			try {
				if (completed) {
					return false;
				}
				if (!onSubscribeCalled) {
					throw new SubscriptionIllegalStateException("OnError must be called only after onSubscribe");
				}
				if (onCompleteCalled) {
					throw new SubscriptionIllegalStateException("OnError must not be called after onComplete");
				}
			}
			finally {
				subscriptionLock.unlock();
			}

			return subscriber.onError(throwable);
		}

		@Override
		public void onComplete(CompletionType completionType) {
			subscriptionLock.lock();
			try {
				if (!onSubscribeCalled) {
					throw new SubscriptionIllegalStateException("OnComplete must be called only after onSubscribe");
				}
				if (onCompleteCalled) {
					throw new SubscriptionIllegalStateException("OnComplete must be called only once");
				}
				onCompleteCalled = true;
			}
			finally {
				subscriptionLock.unlock();
			}

			subscriber.onComplete(completionType);
		}

		@Override
		public boolean isCompleted() {
			return completed;
		}

		public void cancel(CompletionType completionType) {
			subscriptionLock.lock();
			try {
				completed = true;
				boolean removed = subscriptions.remove(this);
				if (removed) {
					onComplete(completionType);
				}
			}
			finally {
				subscriptionLock.unlock();
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
