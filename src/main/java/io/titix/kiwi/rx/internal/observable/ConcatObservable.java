package io.titix.kiwi.rx.internal.observable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 24-Feb-19
 */
public final class ConcatObservable<T> extends BaseObservable<T> {

	private final BaseObservable<T>[] observables;

	@SuppressWarnings("unchecked")
	public ConcatObservable(Observable<? extends T>[] observables) {
		BaseObservable<T>[] baseObservables = new BaseObservable[observables.length];
		int index = 0;
		for (Observable<? extends T> observable : observables) {
			if (observable instanceof BaseObservable) {
				baseObservables[index++] = (BaseObservable<T>) observable;
			}
			else {
				throw new UnsupportedOperationException("It is not for your implementation :)");
			}
		}
		this.observables = baseObservables;
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
		for (BaseObservable<T> observable : observables) {
			observable.onComplete(() -> {
				if (count.decrementAndGet() == 0) {
					runnable.run();
				}
			});
		}
	}
}
