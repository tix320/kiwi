package com.gitlab.tixtix320.kiwi.observable.decorator.single.reduce.collect.internal;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.Observer;
import com.gitlab.tixtix320.kiwi.observable.Subscription;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.reduce.internal.ReduceObservable;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class CollectorObservable<S, R> extends ReduceObservable<S, R> {

	private final Queue<S> objects;

	CollectorObservable(Observable<S> observable) {
		super(observable);
		this.objects = new ConcurrentLinkedQueue<>();
	}

	@Override
	public final Subscription subscribeAndHandle(Observer<? super R> observer) {
		Subscription subscription = observable.subscribeAndHandle(objects::add);
		Subscription completeSubscription = observable.onComplete(() -> observer.consume(collect(objects.stream())));
		return () -> {
			subscription.unsubscribe();
			completeSubscription.unsubscribe();
		};
	}

	protected abstract R collect(Stream<S> objects);
}
