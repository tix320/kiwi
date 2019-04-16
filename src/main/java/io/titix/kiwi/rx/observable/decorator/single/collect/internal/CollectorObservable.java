package io.titix.kiwi.rx.observable.decorator.single.collect.internal;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import io.titix.kiwi.rx.observable.Observable;
import io.titix.kiwi.rx.observable.Observer;
import io.titix.kiwi.rx.observable.ObserverWithSubscription;
import io.titix.kiwi.rx.observable.Subscription;
import io.titix.kiwi.rx.observable.decorator.single.internal.SingleDecoratorObservable;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class CollectorObservable<S, R> extends SingleDecoratorObservable<S, R> {

	private final Queue<S> objects;

	CollectorObservable(Observable<S> observable) {
		super(observable);

		this.objects = new ConcurrentLinkedQueue<>();
		observable.subscribe(objects::add);
	}

	@Override
	public final Subscription subscribe(Observer<? super R> observer) {
		final AtomicBoolean subscribed = new AtomicBoolean(true);
		observable.onComplete(() -> {
			if (subscribed.get()) {
				observer.consume(collect(objects.stream()));
			}
		});
		return () -> subscribed.set(false);
	}

	@Override
	public final Subscription subscribeAndHandle(ObserverWithSubscription<? super R> observer) {
		final AtomicBoolean subscribed = new AtomicBoolean(true);
		observable.onComplete(() -> {
			if (subscribed.get()) {
				observer.consume(collect(objects.stream()), () -> subscribed.set(false));
			}
		});
		return () -> subscribed.set(false);
	}

	protected abstract R collect(Stream<S> objects);
}
