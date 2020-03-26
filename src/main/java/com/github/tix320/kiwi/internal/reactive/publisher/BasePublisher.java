package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import com.github.tix320.kiwi.api.reactive.observable.*;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.IDGenerator;
import com.github.tix320.kiwi.internal.reactive.CompletedException;
import com.github.tix320.kiwi.internal.reactive.observable.BaseObservable;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BasePublisher<T> implements Publisher<T> {

	private final IDGenerator subscriberIdGenerator;

	private final AtomicBoolean completed;

	private final Collection<InternalSubscription> subscriptions;

	private final Lock publishLock;

	protected BasePublisher() {
		this.subscriberIdGenerator = new IDGenerator(1);
		this.completed = new AtomicBoolean(false);
		this.subscriptions = new ConcurrentLinkedQueue<>();
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
		publishLock.lock();
		try {
			failIfCompleted();
			for (InternalSubscription subscription : subscriptions) {
				subscription.cancel(CompletionType.SOURCE_COMPLETED);
			}
			completed.set(true);
		}
		finally {
			publishLock.unlock();
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
		Collection<InternalSubscription> subscriptions;
		publishLock.lock();
		try {
			failIfCompleted();

			subscriptions = getSubscriptionsCopy();
			if (isNormal) {
				prePublish((T) object);
			}
		}
		finally {
			publishLock.unlock();
		}

		for (InternalSubscription subscription : subscriptions) {
			try {
				publishObjectToOneSubscriber(subscription, object, isNormal);
			}
			catch (Exception e) {
				e.printStackTrace();
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
			throw new CompletedException("Publisher is completed, you can not complete again or publish items.");
		}
	}

	private Collection<InternalSubscription> getSubscriptionsCopy() {
		return subscriptions.stream().filter(subscription -> !subscription.completed).collect(Collectors.toList());
	}

	public final class PublisherObservable extends BaseObservable<T> {

		@Override
		public void subscribe(Subscriber<? super T> subscriber) {
			InternalSubscription subscription;
			publishLock.lock();
			AtomicBoolean needMoreHolder = new AtomicBoolean(true);
			try {
				subscription = new InternalSubscription(subscriber);
				subscriptions.add(subscription);
				subscription.onSubscribe(subscription);

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
		public void onSubscribe(Subscription subscription) {
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

			subscriber.onSubscribe(subscription);
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
