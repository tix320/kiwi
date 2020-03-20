package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Collection;

import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

public final class MonoPublisher<T> extends BasePublisher<T> {

	private volatile T value;

	@Override
	protected boolean onSubscribe(InternalSubscription subscription) {
		if (value != null) {
			subscription.onPublish(value);
			return false;
		}
		else {
			return true;
		}
	}

	@Override
	protected void publishOverride(Collection<InternalSubscription> subscriptions, T object) {
		value = object;
		for (InternalSubscription subscription : subscriptions) {
			try {
				subscription.onPublish(object);
				subscription.unsubscribe();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		complete();
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
