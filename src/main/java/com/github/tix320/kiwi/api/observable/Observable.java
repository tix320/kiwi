package com.github.tix320.kiwi.api.observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.github.tix320.kiwi.internal.observable.decorator.multiple.CombineObservable;
import com.github.tix320.kiwi.internal.observable.decorator.multiple.ConcatObservable;
import com.github.tix320.kiwi.internal.observable.subject.BufferPublisher;
import com.github.tix320.kiwi.internal.observable.subject.SimplePublisher;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public interface Observable<T> {

	/**
	 * Subscribe to observable.
	 * If observable already completed, then available values will be processed immediately.
	 */
	Subscription subscribe(Consumer<? super T> consumer);

	/**
	 * Subscribe to observable and handle every item, consumer must return boolean value,
	 * which indicates that need more elements or not.
	 * If observable already completed, then available values will be processed immediately.
	 */
	Subscription subscribeAndHandle(ConditionalConsumer<? super Item<? extends T>> consumer);

	/**
	 * Add handler on completion of observable.
	 * If observable already completed, then handler be processed immediately.
	 */
	void onComplete(Runnable runnable);

	/**
	 * Returns observable, which will subscribe to this and blocks current thread until it will be completed or will not want more items.
	 */
	Observable<T> waitComplete();

	/**
	 * Return observable, which will subscribe to this and receive n objects, after that unsubscribe.
	 *
	 * @param count for wanted objects
	 *
	 * @return new observable
	 */
	Observable<T> take(long count);

	/**
	 * Return observable, which will subscribe to given observable
	 * and unsubscribe from this observable, when given will be completed.
	 *
	 * @return new observable
	 */
	Observable<T> takeUntil(Observable<?> observable);

	/**
	 * Equivalent to {@link Observable#take(long)} with value 1
	 *
	 * @return new observable
	 */
	Observable<T> one();

	/**
	 * Return observable, which will subscribe to this and transform every object according to given transformer.
	 *
	 * @param mapper for transform objects
	 * @param <R>    type of result object
	 *
	 * @return new observable
	 */
	<R> Observable<R> map(Function<? super T, ? extends R> mapper);


	/**
	 * Return observable, which will subscribe to this and set filter to objects according to given filter.
	 *
	 * @param filter for filtering objects
	 *
	 * @return new observable
	 */
	Observable<T> filter(Predicate<? super T> filter);

	/**
	 * Return observable, which will subscribe to this and wait until it will be completed,
	 * and collect received values to map according to given factories.
	 *
	 * @param keyMapper   for extracting map key from objects
	 * @param valueMapper for extracting map value from objects
	 * @param <K>         type of map key
	 * @param <V>         type of map value
	 *
	 * @return new observable
	 */
	<K, V> Observable<Map<K, V>> toMap(Function<? super T, ? extends K> keyMapper,
									   Function<? super T, ? extends V> valueMapper);

	/**
	 * Return observable, which will subscribe to this and wait until it will be completed,
	 * and collect received values to list.
	 *
	 * @return new observable
	 */
	Observable<List<T>> toList();

	/**
	 * Return observable, which will subscribe to this and wait until it will be completed,
	 * and join received values string representations.
	 *
	 * @param toString  to transform object to string
	 * @param delimiter to separate transformed strings
	 *
	 * @return new observable
	 */
	Observable<String> join(Function<? super T, ? extends String> toString, String delimiter);

	/**
	 * Return observable, which will subscribe to this and wait until it will be completed,
	 * and join received values string representations.
	 *
	 * @param toString  to transform object to string
	 * @param delimiter to separate transformed strings
	 * @param prefix    to concat before strings join
	 * @param suffix    to concat after string join
	 *
	 * @return new observable
	 */
	Observable<String> join(Function<? super T, ? extends String> toString, String delimiter, String prefix,
							String suffix);

	/**
	 * Return empty observable, which will be immediately completed.
	 *
	 * @return observable
	 */
	static <T> Observable<T> empty() {
		SimplePublisher<T> subject = new SimplePublisher<>();
		subject.complete();
		return subject.asObservable();
	}

	/**
	 * Return observable, which will be produce given object and then immediately completed.
	 *
	 * @param value to publish
	 * @param <T>   type of object
	 *
	 * @return observable
	 */
	static <T> Observable<T> of(T value) {
		BufferPublisher<T> subject = new BufferPublisher<>(1);
		subject.publish(value);
		subject.complete();
		return subject.asObservable();
	}


	/**
	 * Return observable, which will be produce given objects and then immediately completed.
	 *
	 * @param values to publish
	 * @param <T>    type of object
	 *
	 * @return observable
	 */
	@SafeVarargs
	static <T> Observable<T> of(T... values) {
		BufferPublisher<T> subject = new BufferPublisher<>(values.length);
		subject.publish(values);
		subject.complete();
		return subject.asObservable();
	}

	/**
	 * Return observable, which will be subscribe to given observables and publish objects from each of them.
	 *
	 * @param observables to subscribe
	 * @param <T>         type of object
	 *
	 * @return observable
	 */
	@SafeVarargs
	static <T> Observable<T> concat(Observable<T>... observables) {
		List<Observable<T>> list = new ArrayList<>(Arrays.asList(observables));
		return new ConcatObservable<>(list);
	}

	/**
	 * Return observable, which will be subscribe to given observables and publish objects from each of them.
	 *
	 * @param observables to subscribe
	 * @param <T>         type of object
	 *
	 * @return observable
	 */
	static <T> Observable<T> concat(Iterable<Observable<T>> observables) {
		List<Observable<T>> list = new ArrayList<>();
		for (Observable<T> observable : observables) {
			list.add(observable);
		}
		return new ConcatObservable<>(list);
	}

	/**
	 * Return observable, which will be subscribe to given observables.
	 * It will ba wait for objects from every observable and then combine them to list and publish.
	 *
	 * @param observables to subscribe
	 * @param <T>         type of object
	 *
	 * @return observable
	 */
	@SafeVarargs
	static <T> Observable<List<T>> combine(Observable<T>... observables) {
		List<Observable<T>> list = new ArrayList<>(Arrays.asList(observables));
		return new CombineObservable<>(list);
	}

	/**
	 * Return observable, which will be subscribe to given observables.
	 * It will ba wait for objects from every observable and then combine them to list and publish.
	 *
	 * @param observables to subscribe
	 * @param <T>         type of object
	 *
	 * @return observable
	 */
	static <T> Observable<List<T>> combine(Iterable<Observable<T>> observables) {
		List<Observable<T>> list = new ArrayList<>();
		for (Observable<T> observable : observables) {
			list.add(observable);
		}
		return new CombineObservable<>(list);
	}
}
