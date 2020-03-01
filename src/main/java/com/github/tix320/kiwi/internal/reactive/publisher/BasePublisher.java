package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

	private boolean completed;

	private final List<InternalSubscription> subscriptions;

	protected BasePublisher() {
		this.subscriptions = new ArrayList<>();
	}

	@Override
	public final synchronized void publishError(Throwable throwable) {
		checkCompleted();

		List<InternalSubscription> subscriptions = getSubscriptions();
		for (int i = 0; i < subscriptions.size(); i++) {
			InternalSubscription subscription = subscriptions.get(i);
			try {
				boolean needMore = subscription.onError(throwable);
				if (!needMore) {
					subscription.unsubscribe();
					i--;
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public final synchronized void complete() {
		if (!completed) {
			while (!subscriptions.isEmpty()) {
				try {
					InternalSubscription subscription = subscriptions.get(0);
					subscription.unsubscribe();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		completed = true;
	}

	@Override
	public final Observable<T> asObservable() {
		return new PublisherObservable();
	}

	protected void checkCompleted() {
		if (completed) {
			throw new CompletedException("Publisher is completed, you can not subscribe to it or publish items.");
		}
	}

	protected abstract boolean onSubscribe(InternalSubscription subscription);

	protected List<InternalSubscription> getSubscriptions() {
		return Collections.unmodifiableList(subscriptions);
	}

	private final class PublisherObservable extends BaseObservable<T> {

		@Override
		public Subscription subscribe(Subscriber<? super T> subscriber) {
			synchronized (BasePublisher.this) {
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
	}

	protected final class InternalSubscription implements Subscriber<T>, Subscription {

		private final long id;
		private final Subscriber<? super T> subscriber;

		private InternalSubscription(Subscriber<? super T> subscriber) {
			this.subscriber = subscriber;
			this.id = ID_GEN.next();
		}

		@Override
		public void onSubscribe(Subscription subscription) {
			subscriber.onSubscribe(subscription);
		}

		@Override
		public boolean onPublish(T item) {
			return subscriber.onPublish(item);
		}

		@Override
		public boolean onError(Throwable throwable) {
			return subscriber.onError(throwable);
		}

		@Override
		public void onComplete() {
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
				InternalSubscription.this.onComplete();
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
