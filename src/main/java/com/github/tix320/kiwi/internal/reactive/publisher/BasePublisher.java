package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

	private final IDGenerator ID_GEN = new IDGenerator();

	private volatile boolean completed;

	private final Collection<InternalSubscription> subscriptions;

	private final Lock lock;

	protected BasePublisher() {
		this.subscriptions = new ConcurrentLinkedQueue<>();
		this.lock = new ReentrantLock();
	}

	@Override
	public final void publishError(Throwable throwable) {
		runInLock(() -> {
			failIfCompleted();

			Collection<InternalSubscription> subscriptions = getSubscriptionsCopy();
			for (InternalSubscription subscription : subscriptions) {
				try {
					boolean needMore = subscription.onError(throwable);
					if (!needMore) {
						subscription.unsubscribe();
					}
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public final void complete() {
		runInLock(() -> {
			failIfCompleted();

			Collection<InternalSubscription> subscriptions = getSubscriptionsCopy();
			for (InternalSubscription subscription : subscriptions) {
				subscription.unsubscribe();
			}
			completed = true;
		});
	}

	@Override
	public Observable<T> asObservable() {
		return new PublisherObservable();
	}

	protected void failIfCompleted() {
		if (completed) {
			throw new CompletedException("Publisher is completed, you can not subscribe to it or publish items.");
		}
	}

	protected abstract boolean onSubscribe(InternalSubscription subscription);

	protected Collection<InternalSubscription> getSubscriptionsCopy() {
		return new LinkedList<>(subscriptions);
	}

	public final class PublisherObservable extends BaseObservable<T> {

		@Override
		public Subscription subscribe(Subscriber<? super T> subscriber) {
			InternalSubscription subscription = new InternalSubscription(subscriber);
			subscriptions.add(subscription);

			subscription.onSubscribe(subscription);

			boolean needRegister = BasePublisher.this.onSubscribe(subscription);

			if (!needRegister || completed) {
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

		private InternalSubscription(Subscriber<? super T> subscriber) {
			this.subscriber = subscriber;
			this.id = ID_GEN.next();
			this.onSubscribeCalled = false;
			this.onCompleteCalled = false;
		}

		@Override
		public void onSubscribe(Subscription subscription) {
			if (onSubscribeCalled) {
				throw new IllegalStateException("OnSubscribe must be called only once");
			}
			onSubscribeCalled = true;
			subscriber.onSubscribe(subscription);
		}

		@Override
		public boolean onPublish(T item) {
			if (!onSubscribeCalled) {
				throw new IllegalStateException("OnPublish must be called only after onSubscribe");
			}
			if (onCompleteCalled) {
				throw new IllegalStateException("OnPublish must not be called after onComplete");
			}
			return subscriber.onPublish(item);
		}

		@Override
		public boolean onError(Throwable throwable) {
			if (!onSubscribeCalled) {
				throw new IllegalStateException("OnError must be called only after onSubscribe");
			}
			if (onCompleteCalled) {
				throw new IllegalStateException("OnError must not be called after onComplete");
			}
			return subscriber.onError(throwable);
		}

		@Override
		public void onComplete() {
			if (!onSubscribeCalled) {
				throw new IllegalStateException("OnComplete must be called only after onSubscribe");
			}
			if (onCompleteCalled) {
				throw new IllegalStateException("OnComplete must be called only once");
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
			boolean removed = subscriptions.remove(InternalSubscription.this);
			if (removed) {
				runAsync(InternalSubscription.this::onComplete);
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

	protected void runInLock(Runnable runnable) {
		try {
			lock.lock();
			runnable.run();
		}
		finally {
			lock.unlock();
		}
	}

	protected void runAsync(Runnable runnable) {
		runnable.run();
	}
}
