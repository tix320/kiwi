package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
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

	protected final Collection<Subscriber<? super T>> subscribers;

	protected BasePublisher() {
		this.subscribers = new LinkedList<>();
	}

	@Override
	public final synchronized void publishError(Throwable throwable) {
		Iterator<Subscriber<? super T>> iterator = subscribers.iterator();
		while (iterator.hasNext()) {
			Subscriber<? super T> subscriber = iterator.next();
			boolean needMore;
			try {
				needMore = subscriber.onError(throwable);
				if (!needMore) {
					iterator.remove();
					subscriber.onComplete();
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
			subscribers.forEach(Subscriber::onComplete);
			completed = true;
			subscribers.clear();
		}
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

	protected abstract boolean onSubscribe(Subscriber<? super T> subscriber);

	private final class PublisherObservable extends BaseObservable<T> {

		@Override
		public Subscription subscribe(Subscriber<? super T> subscriber) {
			synchronized (BasePublisher.this) {
				Subscriber<? super T> subscriberWithId = new SubscriberWithId(subscriber);
				boolean needRegister = BasePublisher.this.onSubscribe(subscriberWithId);

				if (completed) {
					subscriberWithId.onComplete();
				}
				if (!needRegister) {
					return () -> {};
				}
				subscribers.add(subscriberWithId);
				return () -> {
					boolean removed = subscribers.remove(subscriberWithId);
					if (removed) {
						subscriberWithId.onComplete();
					}
				};
			}
		}
	}

	private final class SubscriberWithId implements Subscriber<T> {

		private final long id;
		private final Subscriber<? super T> subscriber;

		private SubscriberWithId(Subscriber<? super T> subscriber) {
			this.subscriber = subscriber;
			this.id = ID_GEN.next();
		}

		@Override
		public boolean consume(T item) {
			return subscriber.consume(item);
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
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			@SuppressWarnings("unchecked")
			SubscriberWithId that = (SubscriberWithId) o;
			return id == that.id;
		}

		@Override
		public int hashCode() {
			return Objects.hash(id);
		}
	}
}
