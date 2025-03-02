package com.github.tix320.kiwi.publisher;

import com.github.tix320.kiwi.observable.MonoObservable;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.signal.PublishSignal;
import com.github.tix320.kiwi.publisher.internal.BasePublisher;
import com.github.tix320.kiwi.publisher.internal.PublisherCursor;

/**
 * Mono publisher to publish exactly one object, after which the publisher will be closed.
 * The subscribers will receive that object after subscription immediately.
 */
public final class MonoPublisher<T> extends BasePublisher<T> {

	private volatile PublishSignal<T> valueSignal;

	public MonoPublisher() {
		super();
	}

	@Override
	public MonoObservable<T> asObservable() {
		Observable<T> observable = super.asObservable();
		return new MonoObservable<>() {
			@Override
			public void subscribe(Subscriber<? super T> subscriber) {
				observable.subscribe(subscriber);
			}
		};
	}

	@Override
	protected PublisherCursor createCursor() {
		return new PublisherCursor() {

			volatile boolean alreadyReceived;

			@Override
			public boolean hasNext() {
				return !alreadyReceived && valueSignal != null;
			}

			@Override
			public PublishSignal<T> next() {
				if (alreadyReceived || valueSignal == null) {
					return null;
				}

				alreadyReceived = true;
				return valueSignal;
			}
		};
	}

	@Override
	protected void onPublish(T item) {
		valueSignal = new PublishSignal<>(item);
		MonoPublisher.this.complete(SourceCompletion.DEFAULT);
	}

}
