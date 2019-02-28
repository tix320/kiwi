package io.titix.kiwi.rx.internal.observer;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 24-Feb-19
 */
public final class ConcatObservable<T> implements Observable<T> {

	private final Observable<T>[] observables;

	public ConcatObservable(Observable<T>[] observables) {
		this.observables = observables;
	}

	@Override
	public Subscription subscribe(Consumer<? super T> consumer) {
		Subscription[] subscriptions = new Subscription[observables.length];
		for (int i = 0; i < observables.length; i++) {
			subscriptions[i] = observables[i].subscribe(consumer);
		}
		return () -> {
			for (Subscription subscription : subscriptions) {
				subscription.unsubscribe();
			}
		};
	}

	@Override
	public void onComplete(Runnable runnable) {
		AtomicInteger count = new AtomicInteger(observables.length);
		for (Observable<T> observable : observables) {
			observable.onComplete(() -> {
				if (count.decrementAndGet() == 0) {
					runnable.run();
				}
			});
		}
	}
}
