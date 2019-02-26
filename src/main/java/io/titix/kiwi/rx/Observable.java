package io.titix.kiwi.rx;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import io.titix.kiwi.rx.internal.observer.ConcatObservable;
import io.titix.kiwi.rx.internal.observer.IntervalObservable;
import io.titix.kiwi.rx.internal.observer.decorator.CountingObservable;
import io.titix.kiwi.rx.internal.observer.decorator.MapObservable;
import io.titix.kiwi.rx.internal.observer.decorator.OneTimeObservable;
import io.titix.kiwi.rx.internal.observer.decorator.UntilObservable;
import io.titix.kiwi.rx.internal.subject.buffer.ConcurrentBufferSubject;

/**
 * @author tix32 on 21-Feb-19
 */
public interface Observable<T> {

	Subscription subscribe(Consumer<T> consumer);

	default Observable<T> take(long count) {
		return new CountingObservable<>(this, count);
	}

	default Observable<T> takeUntil(Observable<?> observable) {
		return new UntilObservable<>(this, observable);
	}

	default Observable<T> one() {
		return new OneTimeObservable<>(this);
	}

	default <R> Observable<R> map(Function<T, R> mapper) {
		return new MapObservable<>(this, mapper);
	}

	static <T> Observable<T> of(T value) {
		ConcurrentBufferSubject<T> subject = new ConcurrentBufferSubject<>(1);
		subject.next(value);
		return subject.asObservable();
	}

	@SafeVarargs
	static <T> Observable<T> of(T... values) {
		ConcurrentBufferSubject<T> subject = new ConcurrentBufferSubject<>(values.length);
		subject.next(values);
		return subject.asObservable();
	}

	static <T> InfiniteObservable<T> interval(Supplier<T> supplier, long value, TimeUnit timeUnit) {
		return new IntervalObservable<>(supplier, value, timeUnit);
	}

	@SafeVarargs
	static <T> Observable<T> concat(Observable<T>... observables) {
		return new ConcatObservable<>(observables);
	}
}
