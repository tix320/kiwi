package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Iterator;
import java.util.Objects;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;
import com.github.tix320.kiwi.internal.reactive.publisher.SubscriberException;

public final class MonoPublisher<T> extends BasePublisher<T> {

	private volatile T value;

	@Override
	protected boolean onNewSubscriber(InternalSubscription subscription) {
		if (value != null) {
			return subscription.onPublish(value);
		}
		return true;
	}

	@Override
	protected void prePublish(Object object, boolean isNormal) {
		if (isNormal) {
			value = Objects.requireNonNull((T) object);
		}
	}

	@Override
	protected void publishObject(Object object, boolean isNormal) {
		/* this iterator is based on array snapshot, due the CopyOnWriteArrayList implementation
			 and why we are creating it outside the lock? because Publisher specification requires to publish on that subscribers, which exists in moment of publish
			therefore in case to wait until lock free, array may be changed
			*/
		Iterator<InternalSubscription> subscriptionIterator = subscriptions.iterator();

		publishLock.lock();
		try {
			checkCompleted();
			completed.set(true);

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

		for (InternalSubscription subscription : subscriptions) {
			subscription.cancel(CompletionType.SOURCE_COMPLETED);
		}
	}

	@Override
	public MonoObservable<T> asObservable() {
		PublisherObservable publisherObservable = new PublisherObservable();
		return publisherObservable::subscribe;
	}

	public T getValue() {
		return value;
	}
}
