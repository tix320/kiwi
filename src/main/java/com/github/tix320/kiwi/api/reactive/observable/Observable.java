package com.github.tix320.kiwi.api.reactive.observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.reactive.publisher.BufferPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.SimplePublisher;
import com.github.tix320.kiwi.internal.reactive.observable.UnhandledObservableException;
import com.github.tix320.kiwi.internal.reactive.observable.transform.multiple.ConcatObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.multiple.ZipObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.WaitCompleteObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.collect.JoinObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.collect.ToListObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.collect.ToMapObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator.*;

/**
 * @param <T> type of data.
 *
 * @author Tigran Sargsyan on 21-Feb-19
 */
public interface Observable<T> {


	/**
	 * Subscribe to observable.
	 * If observable already completed, then available values will be processed immediately.
	 *
	 * @param consumer for processing items
	 *
	 * @return object, for controlling subscription in future
	 */
	default Subscription subscribe(Consumer<? super T> consumer) {
		return subscribe(consumer, throwable -> {
			throw new UnhandledObservableException(throwable);
		});
	}


	/**
	 * Subscribe to observable.
	 * If observable already completed, then available values will be processed immediately.
	 *
	 * @param consumer   for processing items
	 * @param onComplete action on complete
	 *
	 * @return object, for controlling subscription in future
	 */
	default Subscription subscribe(Consumer<? super T> consumer, Runnable onComplete) {
		return subscribe(new Subscriber<>() {
			@Override
			public boolean consume(T item) {
				consumer.accept(item);
				return true;
			}

			@Override
			public boolean onError(Throwable throwable) {
				throw new UnhandledObservableException(throwable);
			}

			@Override
			public void onComplete() {
				onComplete.run();
			}
		});
	}

	/**
	 * Subscribe to observable.
	 * If observable already completed, then available values will be processed immediately.
	 * If any error occurs, then errorHandler will be invoked.
	 *
	 * @param consumer     for processing items
	 * @param errorHandler for handling errors
	 *
	 * @return object, for controlling subscription in future
	 */
	default Subscription subscribe(Consumer<? super T> consumer, ConditionalConsumer<Throwable> errorHandler) {
		return particularSubscribe(item -> {
			consumer.accept(item);
			return true;
		}, errorHandler);
	}

	/**
	 * Subscribe to observable and handle every item, consumer must return boolean value,
	 * which indicates that need more elements or not.
	 * If observable already completed, then available values will be processed immediately.
	 *
	 * @param consumer for processing items
	 *
	 * @return object, for controlling subscription in future
	 */
	default Subscription particularSubscribe(ConditionalConsumer<? super T> consumer) {
		return particularSubscribe(consumer, throwable -> {
			throw new UnhandledObservableException(throwable);
		});
	}

	/**
	 * Subscribe to observable and handle every item, consumer must return boolean value,
	 * which indicates that need more elements or not.
	 * If observable already completed, then available values will be processed immediately.
	 * If any error occurs, then errorHandler will be invoked.
	 *
	 * @param consumer     for processing items
	 * @param errorHandler for handling errors
	 *
	 * @return object, for controlling subscription in future
	 */
	default Subscription particularSubscribe(ConditionalConsumer<? super T> consumer,
											 ConditionalConsumer<Throwable> errorHandler) {
		return subscribe(new Subscriber<>() {
			@Override
			public boolean consume(T item) {
				return consumer.consume(item);
			}

			@Override
			public boolean onError(Throwable throwable) {
				return errorHandler.consume(throwable);
			}

			@Override
			public void onComplete() {

			}
		});
	}

	/**
	 * Subscribe to observable and handle every item, error or completeness.
	 * If observable already completed, then available values will be processed immediately
	 * and after which completed handler will be invoked.
	 *
	 * @param subscriber for subscribing
	 *
	 * @return object, for controlling subscription in future
	 */
	Subscription subscribe(Subscriber<? super T> subscriber);

	/**
	 * Blocks current thread until this observable will be completed or will not want more items.
	 */
	default void blockUntilComplete() {
		await().subscribe(t -> {});
	}

	/**
	 * Blocks current thread until this observable will be published one value and return.
	 */
	default T get() {
		CountDownLatch latch = new CountDownLatch(1);

		AtomicReference<T> itemHolder = new AtomicReference<>();
		this.toMono().subscribe(item -> {
			itemHolder.set(item);
			latch.countDown();
		});

		Try.runOrRethrow(latch::await);
		return itemHolder.get();
	}


	// transforming functions --------------------------------------

	/**
	 * Returns observable, which subscription will blocks thread until it will be completed or will not want more items.
	 *
	 * @return new observable
	 */
	default Observable<T> await() {
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
	 * @param observable to subscribe
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
	 * @param <T> type of observable
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
	static <T> Observable<List<T>> zip(Observable<T>... observables) {
		List<Observable<T>> list = new ArrayList<>(Arrays.asList(observables));
		return new ZipObservable<>(list);
	}

	/**
	 * Return observable, which will be subscribe to given observables.
	 * It will ba wait for objects from every observable and then combine them to list and publish.
	 * Items order in list will be same as a given observables.
	 *
	 * @param observables to subscribe
	 * @param <T>         type of object
	 *
	 * @return observable
	 */
	static <T> Observable<List<T>> zip(Iterable<Observable<T>> observables) {
		List<Observable<T>> list = new ArrayList<>();
		for (Observable<T> observable : observables) {
			list.add(observable);
		}
		return new ZipObservable<>(list);
	}
}
