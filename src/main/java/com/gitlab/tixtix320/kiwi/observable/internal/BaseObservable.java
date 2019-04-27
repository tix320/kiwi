package com.gitlab.tixtix320.kiwi.observable.internal;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Exchanger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import com.gitlab.tixtix320.kiwi.check.Try;
import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.Subscription;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.collect.internal.JoinObservable;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.collect.internal.ToListObservable;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.collect.internal.ToMapObservable;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.transform.internal.*;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public abstract class BaseObservable<T> implements Observable<T> {

	public final T get() {
		Observable<T> observable = this.one();
		Exchanger<T> exchanger = new Exchanger<>();
		CompletableFuture.runAsync(
				() -> observable.subscribe(value -> Try.runAndRethrow(() -> exchanger.exchange(value))));
		return Try.supplyAndGet(() -> exchanger.exchange(null));
	}

	public final Observable<T> take(long count) {
		return new CountingObservable<>(this, count);
	}

	public final Observable<T> takeUntil(Observable<?> observable) {
		return new UntilObservable<>(this, observable);
	}

	public final Observable<T> one() {
		return new OnceObservable<>(this);
	}

	public final <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
		return new MapperObservable<>(this, mapper);
	}

	public final Observable<T> filter(Predicate<? super T> filter) {
		return new FilterObservable<>(this, filter);
	}

	public final <K, V> Observable<Map<K, V>> toMap(Function<? super T, ? extends K> keyMapper,
													Function<? super T, ? extends V> valueMapper) {
		return new ToMapObservable<>(this, keyMapper, valueMapper);
	}

	public final <R> Observable<R> transform(BiFunction<Subscription, T, Optional<R>> transformer) {
		return new TransformObservable<>(this) {
			@Override
			protected BiFunction<Subscription, T, Optional<R>> transformer() {
				return transformer;
			}
		};
	}

	public final Observable<List<T>> toList() {
		return new ToListObservable<>(this);
	}

	public final Observable<String> join(Function<? super T, ? extends String> toString, String delimiter) {
		return new JoinObservable<>(this, toString, delimiter);
	}

	public final Observable<String> join(Function<? super T, ? extends String> toString, String delimiter,
										 String prefix, String suffix) {
		return new JoinObservable<>(this, toString, delimiter, prefix, suffix);
	}
}
