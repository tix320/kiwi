package com.github.tix320.kiwi.internal.reactive.observable.transform.multiple;

import java.util.Collection;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.common.item.Item;
import com.github.tix320.kiwi.api.reactive.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.internal.reactive.observable.transform.TransformObservable;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public final class ConcatObservable<T> extends TransformObservable<T> {

	private final List<Observable<T>> observables;

	public ConcatObservable(List<Observable<T>> observables) {
		this.observables = observables;
	}

	@Override
	public Subscription particularSubscribe(ConditionalConsumer<? super Item<? extends T>> consumer,
											ConditionalConsumer<Throwable> errorHandler) {
		Subscription[] subscriptions = new Subscription[observables.size()];
		for (int i = 0; i < observables.size(); i++) {
			Subscription subscription = observables.get(i).particularSubscribe(consumer, errorHandler);
			subscriptions[i] = subscription;
		}
		return () -> {
			for (Subscription subscription : subscriptions) {
				subscription.unsubscribe();
			}
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Collection<Observable<?>> decoratedObservables() {
		return (Collection) observables;
	}
}
