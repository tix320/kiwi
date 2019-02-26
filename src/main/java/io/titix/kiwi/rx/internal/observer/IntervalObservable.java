package io.titix.kiwi.rx.internal.observer;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.titix.kiwi.rx.InfiniteObservable;
import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subject;
import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.subject.single.ConcurrentSingleSubject;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public final class IntervalObservable<T> implements InfiniteObservable<T> {

	private final Subject<T> subject;

	private final Observable<T> observable;

	private final Supplier<T> supplier;

	private final long millis;

	private final Timer timer;

	public IntervalObservable(Supplier<T> supplier, long value, TimeUnit timeUnit) {
		millis = timeUnit.toMillis(value);
		subject = new ConcurrentSingleSubject<>();
		observable = subject.asObservable();
		this.supplier = supplier;
		this.timer = new Timer(true);
		runTimer();
	}

	private void runTimer() {
		timer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				subject.next(supplier.get());
			}
		}, millis, millis);
	}

	@Override
	public Subscription subscribe(Consumer<T> consumer) {
		return observable.subscribe(consumer);
	}

	@Override
	public void stop() {
		timer.cancel();
	}
}
