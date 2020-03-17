package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Collection;

import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

public class MonoPublisher<T> extends BasePublisher<T> {

	private volatile T value;

	public MonoPublisher() {
	}

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
	public void publish(T object) {
		runInLock(() -> {
			failIfCompleted();
			value = object;
			Collection<InternalSubscription> subscriptions = getSubscriptionsCopy();
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
		});
	}

	@Override
	public void publish(T[] objects) {
		throw new UnsupportedOperationException("Single publisher must publish only one value at once");
	}

	@Override
	public void publish(Iterable<T> iterable) {
		throw new UnsupportedOperationException("Single publisher must publish only one value at once");
	}

	@Override
	public MonoObservable<T> asObservable() {
		PublisherObservable publisherObservable = new PublisherObservable();
		return new MonoObservable<T>() {
			@Override
			public Subscription subscribe(Subscriber<? super T> subscriber) {
				return publisherObservable.subscribe(subscriber);
			}
		};
	}

	public T getValue() {
		return value;
	}
}
