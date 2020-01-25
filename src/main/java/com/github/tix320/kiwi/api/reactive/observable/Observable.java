package com.github.tix320.kiwi.api.reactive.observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.github.tix320.kiwi.api.reactive.common.item.Item;
import com.github.tix320.kiwi.internal.reactive.observable.transform.multiple.CombineObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.multiple.ConcatObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.WaitCompleteObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.collect.JoinObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.collect.ToListObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.collect.ToMapObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator.*;
import com.github.tix320.kiwi.internal.reactive.publisher.BufferPublisher;
import com.github.tix320.kiwi.internal.reactive.publisher.SimplePublisher;

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


	// transforming functions --------------------------------------

	/**
	 * Returns observable, which will subscribe to this and blocks current thread until it will be completed or will not want more items.
	 */
	default Observable<T> waitComplete() {
		return new WaitCompleteObservable<>(this);
	}

	/**
	 * Return observable, which will subscribe to this and receive n objects, after that unsubscribe.
	 *
	 * @param count for wanted objects
	 *
	 * @return new observable
	 */
	default Observable<T> take(long count) {
		return new CountingObservable<>(this, count);
	}

	/**
	 * Return observable, which will subscribe to given observable
	 * and unsubscribe from this observable, when given will be completed.
	 *
	 * @return new observable
	 */
	default Observable<T> takeUntil(Observable<?> observable) {
		return new UntilObservable<>(this, observable);
	}

	/**
	 * Equivalent to {@link Observable#take(long)} with value 1
	 *
	 * @return new observable {@link MonoObservable}
	 */
	default MonoObservable<T> toMono() {
		return new OnceObservable<>(this);
	}

	/**
	 * Return observable, which will subscribe to this and transform every object according to given transformer.
	 *
	 * @param mapper for transform objects
	 * @param <R>    type of result object
	 *
	 * @return new observable
	 */
	default <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
		return new MapperObservable<>(this, mapper);
	}


	/**
	 * Return observable, which will subscribe to this and set filter to objects according to given filter.
	 *
	 * @param filter for filtering objects
	 *
	 * @return new observable
	 */
	default Observable<T> filter(Predicate<? super T> filter) {
		return new FilterObservable<>(this, filter);
	}

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
	default <K, V> Observable<Map<K, V>> toMap(Function<? super T, ? extends K> keyMapper,
											   Function<? super T, ? extends V> valueMapper) {
		return new ToMapObservable<>(this, keyMapper, valueMapper);
	}

	/**
	 * Return observable, which will subscribe to this and wait until it will be completed,
	 * and collect received values to list.
	 *
	 * @return new observable
	 */
	default Observable<List<T>> toList() {
		return new ToListObservable<>(this);
	}

	/**
	 * Return observable, which will subscribe to this and wait until it will be completed,
	 * and join received values string representations.
	 *
	 * @param toString  to transform object to string
	 * @param delimiter to separate transformed strings
	 *
	 * @return new observable
	 */
	default Observable<String> join(Function<? super T, ? extends String> toString, String delimiter) {
		return new JoinObservable<>(this, toString, delimiter);
	}

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
	default Observable<String> join(Function<? super T, ? extends String> toString, String delimiter, String prefix,
									String suffix) {
		return new JoinObservable<>(this, toString, delimiter, prefix, suffix);
	}

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
	static <T> MonoObservable<T> of(T value) {
		BufferPublisher<T> subject = new BufferPublisher<>(1);
		subject.publish(value);
		subject.complete();
		return subject.asObservable().toMono();
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
