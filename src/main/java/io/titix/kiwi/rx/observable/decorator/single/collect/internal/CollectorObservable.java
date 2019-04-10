package io.titix.kiwi.rx.observable.decorator.single.collect.internal;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import io.titix.kiwi.rx.observable.Subscription;
import io.titix.kiwi.rx.observable.decorator.single.internal.SingleDecoratorObservable;
import io.titix.kiwi.rx.observable.internal.BaseObservable;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class CollectorObservable<S, R> extends SingleDecoratorObservable<S, R> {

	private final Queue<S> objects;

	CollectorObservable(BaseObservable<S> observable) {
		super(observable);

		this.objects = new ConcurrentLinkedQueue<>();
		observable.subscribe(objects::add);
	}

	@Override
	public final Subscription subscribe(Consumer<? super R> consumer) {
		final AtomicBoolean subscribed = new AtomicBoolean(true);
		observable.onComplete(() -> {
			if (subscribed.get()) {
				consumer.accept(collect(objects.stream()));
			}
		});
		return () -> subscribed.set(false);
	}

	protected abstract R collect(Stream<S> objects);
}
