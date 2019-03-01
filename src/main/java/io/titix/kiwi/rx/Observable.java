package io.titix.kiwi.rx;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import io.titix.kiwi.rx.internal.observable.collect.ToMapCollector;
import io.titix.kiwi.rx.internal.observable.ConcatObservable;
import io.titix.kiwi.rx.internal.observable.filter.CountingObservable;
import io.titix.kiwi.rx.internal.observable.filter.MapperObservable;
import io.titix.kiwi.rx.internal.observable.filter.OneTimeObservable;
import io.titix.kiwi.rx.internal.observable.filter.UntilObservable;
import io.titix.kiwi.rx.internal.subject.BufferSubject;

/**
 * @author tix32 on 21-Feb-19
 */
public interface Observable<T> {

	Subscription subscribe(Consumer<? super T> consumer);

	default Observable<T> take(long count) {
		return new CountingObservable<>(this, count);
	}

	default Observable<T> takeUntil(Observable<?> observable) {
		return new UntilObservable<>(this, observable);
	}

	default Observable<T> one() {
		return new OneTimeObservable<>(this);
	}

	default <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
		return new MapperObservable<>(this, mapper);
	}

	default <K, V> Observable<Map<K, V>> toMap(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		return new ToMapCollector<>(this, keyMapper, valueMapper);
	}

	static <T> Observable<T> of(T value) {
		BufferSubject<T> subject = new BufferSubject<>(1);
		subject.next(value);
		return subject.asObservable();
	}

	@SafeVarargs
	static <T> Observable<T> of(T... values) {
		BufferSubject<T> subject = new BufferSubject<>(values.length);
		subject.next(values);
		subject.complete();
		return subject.asObservable();
	}

	@SafeVarargs
	static <T> Observable<T> concat(Observable<T>... observables) {
		return new ConcatObservable<>(observables);
	}
}
