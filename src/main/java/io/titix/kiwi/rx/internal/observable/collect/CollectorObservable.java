package io.titix.kiwi.rx.internal.observable.collect;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.observable.BaseObservable;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class CollectorObservable<T, R> extends BaseObservable<R> {

	private final BaseObservable<T> observable;

	private final Queue<T> objects;

	protected CollectorObservable(Observable<T> observable) {
		if (observable instanceof BaseObservable) {
			this.observable = (BaseObservable<T>) observable;
		}
		else {
			throw new UnsupportedOperationException("It is not for your implementation :)");
		}
		this.objects = new ConcurrentLinkedQueue<>();
		observable.subscribe(objects::add);
	}

	@Override
	public final Subscription subscribe(Consumer<? super R> consumer) {
		AtomicBoolean subscribed = new AtomicBoolean(true);
		observable.onComplete(() -> {
			if (subscribed.get()) {
				consumer.accept(collect(objects.stream()));
			}
		});
		return () -> subscribed.set(false);
	}

	@Override
	public final void onComplete(Runnable runnable) {
		observable.onComplete(runnable);
	}

	protected abstract R collect(Stream<T> objects);
}
