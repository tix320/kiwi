package com.github.tix320.kiwi.observable.transform.single.operator.internal;

import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.MinorSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import java.util.Objects;
import java.util.function.Function;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class MapperObservable<S, R> extends Observable<R> {

	private final Observable<S> observable;

	private final Function<? super S, ? extends R> mapper;

	public MapperObservable(Observable<S> observable, Function<? super S, ? extends R> mapper) {
		this.observable = Objects.requireNonNull(observable);
		this.mapper = Objects.requireNonNull(mapper);
	}

	@Override
	public void subscribe(Subscriber<? super R> subscriber) {
		observable.subscribe(subscriber.fork(new MinorSubscriber<S, R>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscriber.setSubscription(subscription);
			}

			@Override
			public void onNext(S item) {
				R mapped;
				try {
					mapped = Objects.requireNonNull(mapper.apply(item), "Mapper result must not be null");
				} catch (Exception exception) {
					replaceDemandAndRethrow(exception);
					return;
				}
				subscriber.publish(mapped);
			}

			@Override
			public void onComplete(Completion completion) {
				subscriber.complete(completion);
			}
		}));
	}

}
