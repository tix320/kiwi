package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
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

	protected BasePublisher() {
		this.subscriberIdGenerator = new IDGenerator(1);
		this.completed = new AtomicBoolean(false);
		this.subscriptions = new ConcurrentLinkedQueue<>();
	}

	@Override
	public final void publish(T object) {
		failIfCompleted();

		Collection<InternalSubscription> subscriptions = getSubscriptionsCopy();
		subscriptions.forEach(subscription -> subscription.currentlyInUse = true);

		publishOverride(subscriptions, object);

		subscriptions.forEach(subscription -> {
			subscription.currentlyInUse = false;
			if (subscription.markForUnsubscribe) {
				subscription.unsubscribe();
			}
		});
	}

	@Override
	public final void publishError(Throwable throwable) {
		failIfCompleted();

		Collection<InternalSubscription> subscriptions = getSubscriptionsCopy();
		subscriptions.forEach(subscription -> subscription.currentlyInUse = true);
		for (InternalSubscription subscription : subscriptions) {
			try {
				boolean needMore = subscription.onError(throwable);
				if (!needMore) {
					subscription.currentlyInUse = false;
					subscription.unsubscribe();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		subscriptions.forEach(subscription -> {
			subscription.currentlyInUse = false;
			if (subscription.markForUnsubscribe) {
				subscription.unsubscribe();
			}
		});
	}

	@Override
	public final void complete() {
		failIfCompleted();

		Collection<InternalSubscription> subscriptions = getSubscriptionsCopy();
		for (InternalSubscription subscription : subscriptions) {
			subscription.unsubscribe();
		}
		completed.set(true);
	}

	@Override
	public final boolean isCompleted() {
		return completed.get();
	}

	@Override
	public Observable<T> asObservable() {
		return new PublisherObservable();
	}

	protected abstract boolean onSubscribe(InternalSubscription subscription);

	protected abstract void publishOverride(Collection<InternalSubscription> subscriptions, T object);

	private void failIfCompleted() {
		if (completed.get()) {
			throw new CompletedException("Publisher is completed, you can not complete again or publish items.");
		}
	}

	private Collection<InternalSubscription> getSubscriptionsCopy() {
		return new ArrayList<>(subscriptions);
	}

	public final class PublisherObservable extends BaseObservable<T> {

		@Override
		public Subscription subscribe(Subscriber<? super T> subscriber) {
			InternalSubscription subscription = new InternalSubscription(subscriber);
			subscriptions.add(subscription);

			subscription.onSubscribe(subscription);

			boolean needRegister = BasePublisher.this.onSubscribe(subscription);

			if (!needRegister || completed.get()) {
				subscription.unsubscribe();
			}

			return subscription;
		}
	}

	protected final class InternalSubscription implements Subscriber<T>, Subscription {

		private final long id;
		private final Subscriber<? super T> subscriber;

		// for validation, antibug
		private volatile boolean onSubscribeCalled;
		private volatile boolean onCompleteCalled;
		public volatile boolean currentlyInUse;
		public volatile boolean markForUnsubscribe;

		private InternalSubscription(Subscriber<? super T> subscriber) {
			this.subscriber = subscriber;
			this.id = subscriberIdGenerator.next();
			this.onSubscribeCalled = false;
			this.onCompleteCalled = false;
			this.currentlyInUse = false;
			this.markForUnsubscribe = false;
		}

		@Override
		public void onSubscribe(Subscription subscription) {
			if (onSubscribeCalled) {
				throw new SubscriptionIllegalStateException("OnSubscribe must be called only once");
			}
			onSubscribeCalled = true;
			subscriber.onSubscribe(subscription);
		}

		@Override
		public boolean onPublish(T item) {
			if (!onSubscribeCalled) {
				throw new SubscriptionIllegalStateException("OnPublish must be called only after onSubscribe");
			}
			if (onCompleteCalled) {
				throw new SubscriptionIllegalStateException("OnPublish must not be called after onComplete");
			}
			return subscriber.onPublish(item);
		}

		@Override
		public boolean onError(Throwable throwable) {
			if (!onSubscribeCalled) {
				throw new SubscriptionIllegalStateException("OnError must be called only after onSubscribe");
			}
			if (onCompleteCalled) {
				throw new SubscriptionIllegalStateException("OnError must not be called after onComplete");
			}
			return subscriber.onError(throwable);
		}

		@Override
		public void onComplete() {
			if (!onSubscribeCalled) {
				throw new SubscriptionIllegalStateException("OnComplete must be called only after onSubscribe");
			}
			if (onCompleteCalled) {
				throw new SubscriptionIllegalStateException("OnComplete must be called only once");
			}
			onCompleteCalled = true;
			subscriber.onComplete();
		}

		@Override
		public boolean isCompleted() {
			return !subscriptions.contains(this);
		}

		@Override
		public void unsubscribe() {
			if (currentlyInUse) {
				markForUnsubscribe = true;
			}
			else {
				boolean removed = subscriptions.remove(this);
				if (removed) {
					onComplete();
				}
			}
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
