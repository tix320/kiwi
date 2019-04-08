package io.titix.kiwi.rx.observable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Exchanger;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import io.titix.kiwi.check.Try;
import io.titix.kiwi.rx.observable.collect.internal.JoinObservable;
import io.titix.kiwi.rx.observable.collect.internal.ToListObservable;
import io.titix.kiwi.rx.observable.collect.internal.ToMapObservable;
import io.titix.kiwi.rx.observable.internal.BaseObservable;
import io.titix.kiwi.rx.observable.internal.ConcatObservable;
import io.titix.kiwi.rx.observable.transform.internal.*;
import io.titix.kiwi.rx.subject.internal.BufferSubject;
import io.titix.kiwi.rx.observable.transform.*;

/**
 * @author tix32 on 21-Feb-19
 */
public interface Observable<T> {

	Subscription subscribe(Consumer<? super T> consumer);

	/**
	 * Waits until will be available one value and return.
	 */
	default T get() {
		Observable<T> observable = castToBase(this).one();
		Exchanger<T> exchanger = new Exchanger<>();
		CompletableFuture.runAsync(
				() -> observable.subscribe(value -> Try.runAndRethrow(() -> exchanger.exchange(value))));
		return Try.supplyAndGet(() -> exchanger.exchange(null));
	}

	default Observable<T> take(long count) {
		return new CountingObservable<>(castToBase(this), count);
	}

	default Observable<T> takeUntil(Observable<?> observable) {
		return new UntilObservable<>(castToBase(this), observable);
	}

	default Observable<T> one() {
		return new OnceObservable<>(castToBase(this));
	}

	default <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
		return new MapperObservable<>(castToBase(this), mapper);
	}

	default Observable<T> filter(Predicate<? super T> filter) {
		return new FilterObservable<>(castToBase(this), filter);
	}

	default <K, V> Observable<Map<K, V>> toMap(Function<? super T, ? extends K> keyMapper,
											   Function<? super T, ? extends V> valueMapper) {
		return new ToMapObservable<>(castToBase(this), keyMapper, valueMapper);
	}

	default <R> Observable<R> transform(BiFunction<Subscription, T, Result<R>> transformer) {
		return new TransformObservable<>(castToBase(this)) {
			@Override
			protected BiFunction<Subscription, T, Result<R>> transformer() {
				return transformer;
			}
		};
	}

	default Observable<List<T>> toList() {
		return new ToListObservable<>(castToBase(this));
	}

	default Observable<String> join(Function<? super T, ? extends String> toString, String delimiter) {
		return new JoinObservable<>(castToBase(this), toString, delimiter);
	}

	default Observable<String> join(Function<? super T, ? extends String> toString, String delimiter, String prefix,
									String suffix) {
		return new JoinObservable<>(castToBase(this), toString, delimiter, prefix, suffix);
	}

	static <T> Observable<T> empty() {
		BufferSubject<T> subject = new BufferSubject<>(0);
		subject.complete();
		return subject.asObservable();
	}

	static <T> Observable<T> of(T value) {
		BufferSubject<T> subject = new BufferSubject<>(1);
		subject.next(value);
		subject.complete();
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
		return new ConcatObservable<>(castToBase(observables));
	}

	private static <T> BaseObservable<T> castToBase(Observable<T> observable) {
		if (observable instanceof BaseObservable) {
			return (BaseObservable<T>) observable;
		}
		throw new UnsupportedOperationException("It is not for your Observable implementation :)");
	}

	@SuppressWarnings("unchecked")
	private static <T> BaseObservable<T>[] castToBase(Observable<T>[] observables) {
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
		return baseObservables;
	}
}
