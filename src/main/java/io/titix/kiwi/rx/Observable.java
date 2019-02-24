package io.titix.kiwi.rx;

import java.util.function.Consumer;
import java.util.function.Function;

import io.titix.kiwi.rx.internal.observer.ConcatObservable;
import io.titix.kiwi.rx.internal.observer.decorator.CountingObservable;
import io.titix.kiwi.rx.internal.observer.decorator.OneTimeObservable;
import io.titix.kiwi.rx.internal.subject.ConcurrentBufferSubject;

/**
 * @author tix32 on 21-Feb-19
 */
public interface Observable<T> {

	Subscription subscribe(Consumer<T> consumer);

	default Observable<T> take(long count) {
		return new CountingObservable<>(this, count);
	}

	default Observable<T> one() {
		return new OneTimeObservable<>(this);
	}

	default <T, R> Observable<R> map(Function<T, R> mapper) {
		return
	}

	static <T> Observable<T> of(T value) {
		ConcurrentBufferSubject<T> subject = new ConcurrentBufferSubject<>(1);
		subject.next(value);
		return subject.asObservable();
	}

	@SafeVarargs
	static <T> Observable<T> concat(Observable<T>... observables) {
		new ConcatObservable<>(observables);
	}
}
