package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import com.github.tix320.kiwi.api.reactive.common.item.Item;
import com.github.tix320.kiwi.api.reactive.common.item.RegularItem;
import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.IDGenerator;
import com.github.tix320.kiwi.internal.reactive.CompletedException;
import com.github.tix320.kiwi.internal.reactive.observable.BaseObservable;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BasePublisher<T> implements Publisher<T> {

	private boolean completed;

	private final Collection<Runnable> onCompleteSubscribers;

	protected final Collection<Subscriber<? super T>> subscribers;

	protected BasePublisher() {
		this.onCompleteSubscribers = new LinkedList<>();
		this.subscribers = new LinkedList<>();
	}

	@Override
	public final synchronized void publishError(Throwable throwable) {
		Iterator<Subscriber<? super T>> iterator = subscribers.iterator();
		while (iterator.hasNext()) {
			Subscriber<? super T> subscriber = iterator.next();
			try {
				boolean needMore = subscriber.consumeError(throwable);
				if (!needMore) {
					iterator.remove();
				}
			}
			catch (Exception e) {
				iterator.remove();
				e.printStackTrace();
			}

		}
	}

	@Override
	public final synchronized void complete() {
		if (!completed) {
			completed = true;
			onCompleteSubscribers.forEach(Runnable::run);
			subscribers.clear();
			onCompleteSubscribers.clear();
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

	protected abstract Subscription subscribe(Subscriber<T> subscriber);

	private final class PublisherObservable extends BaseObservable<T> {

		public PublisherObservable() {
		}

		@Override
		public Subscription particularSubscribe(ConditionalConsumer<? super Item<? extends T>> consumer,
												ConditionalConsumer<Throwable> errorHandler) {
			synchronized (BasePublisher.this) {
				Subscriber<T> subscriber = new Subscriber<>(consumer, errorHandler);
				Subscription subscription = BasePublisher.this.subscribe(subscriber);
				if (completed) {
					subscribers.remove(subscriber);
				}
				return subscription;
			}
		}


		@Override
		public void onComplete(Runnable runnable) {
			synchronized (BasePublisher.this) {
				if (completed) {
					runnable.run();
				}
				else {
					onCompleteSubscribers.add(runnable);
				}
			}
		}
	}

	protected final static class Subscriber<T> {

		private static final IDGenerator GEN = new IDGenerator();

		private final long id;

		private final ConditionalConsumer<? super Item<? extends T>> consumer;

		private final ConditionalConsumer<Throwable> errorConsumer;

		private Subscriber(ConditionalConsumer<? super Item<? extends T>> consumer,
						   ConditionalConsumer<Throwable> errorConsumer) {
			this.errorConsumer = errorConsumer;
			this.id = GEN.next();
			this.consumer = consumer;
		}

		public boolean consume(T object) {
			return consumer.consume(new RegularItem<>(object));
		}

		public boolean consumeError(Throwable throwable) {
			return errorConsumer.consume(throwable);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			Subscriber<?> subscriber = (Subscriber<?>) o;
			return id == subscriber.id;
		}

		@Override
		public int hashCode() {
			return Long.hashCode(id);
		}
	}
}
