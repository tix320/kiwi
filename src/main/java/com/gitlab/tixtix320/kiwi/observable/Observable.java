package com.gitlab.tixtix320.kiwi.observable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import com.gitlab.tixtix320.kiwi.observable.decorator.multiple.internal.ConcatObservable;
import com.gitlab.tixtix320.kiwi.observable.subject.internal.BufferSubject;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public interface Observable<T> {

	Subscription subscribe(Observer<? super T> observer);

	Subscription subscribeAndHandle(ObserverWithSubscription<? super T> observer);

	void onComplete(Runnable runnable);

	/**
	 * Waits until will be available one value and return.
	 */
	T get();

	Observable<T> take(long count);

	Observable<T> takeUntil(Observable<?> observable);

	Observable<T> one();

	<R> Observable<R> map(Function<? super T, ? extends R> mapper);

	Observable<T> filter(Predicate<? super T> filter);

	<K, V> Observable<Map<K, V>> toMap(Function<? super T, ? extends K> keyMapper,
									   Function<? super T, ? extends V> valueMapper);

	<R> Observable<R> transform(BiFunction<Subscription, T, Optional<R>> transformer);

	Observable<List<T>> toList();

	Observable<String> join(Function<? super T, ? extends String> toString, String delimiter);

	Observable<String> join(Function<? super T, ? extends String> toString, String delimiter, String prefix,
							String suffix);

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
		return new ConcatObservable<>(observables);
	}
}
