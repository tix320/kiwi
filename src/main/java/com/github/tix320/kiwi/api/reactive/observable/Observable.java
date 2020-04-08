package com.github.tix320.kiwi.api.reactive.observable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.reactive.publisher.BufferPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.SimplePublisher;
import com.github.tix320.kiwi.api.util.None;
import com.github.tix320.kiwi.api.util.collection.Tuple;
import com.github.tix320.kiwi.internal.reactive.observable.transform.multiple.ConcatObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.multiple.ZipObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.collect.JoinObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.collect.ToListObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.collect.ToMapObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.operator.*;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.timeout.GetOnTimeoutObservable;
import com.github.tix320.kiwi.internal.reactive.observable.transform.single.timeout.WaitCompleteObservable;

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
	 */
	default void subscribe(Consumer<? super T> consumer) {
		subscribe(Subscriber.<T>builder().onPublish(consumer).build());
	}

	/**
	 * Subscribe to observable and handle every item, consumer must return boolean value,
	 * which indicates that need more elements or not.
	 * If observable already completed, then available values will be processed immediately.
	 *
	 * @param consumer for processing items
	 */
	default void conditionalSubscribe(ConditionalConsumer<? super T> consumer) {
		subscribe(Subscriber.<T>builder().onPublishConditional(consumer).build());
	}

	/**
	 * Build subscriber from builder and subscribe.
	 *
	 * @param subscriberBuilder for constructing subscriber
	 *
	 * @see #subscribe(Subscriber)
	 */
	default void subscribe(SubscriberBuilder<? super T> subscriberBuilder) {
		subscribe(subscriberBuilder.build());
	}

	/**
	 * Subscribe to observable and handle every item, error or completeness.
	 * If observable already completed, then available values will be processed immediately
	 * and after which completed handler will be invoked.
	 *
	 * @param subscriber for subscribing
	 */
	void subscribe(Subscriber<? super T> subscriber);

	// await functions --------------------------------------

	/**
	 * Returns observable, which will be block current thread until this observable complete and publish single item.
	 *
	 * @return new observable
	 */
	default Observable<None> await() {
		return await(Duration.ofMillis(-1));
	}

	/**
	 * Returns observable, which will be block current thread until this observable complete and publish single item.
	 *
	 * @param timeout to wait. Note: ceil to milliseconds.
	 *
	 * @return new observable
	 */
	default Observable<None> await(Duration timeout) {
		return new WaitCompleteObservable(this, timeout);
	}

	/**
	 * Blocks current thread until this observable will be completed.
	 */
	default void blockUntilComplete() {
		blockUntilComplete(Duration.ofMillis(-1));
	}

	/**
	 * Blocks current thread for given until this observable will be completed.
	 *
	 * @param timeout timeout to wait. Note: ceil to milliseconds.
	 */
	default void blockUntilComplete(Duration timeout) {
		await(timeout).subscribe(t -> {});
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

	/**
	 * Blocks current thread for given timout until this observable will be published one value and return.
	 *
	 * @param timeout timeout to wait. Note: ceil to milliseconds.
	 */
	default T get(Duration timeout) {
		CountDownLatch latch = new CountDownLatch(1);

		AtomicReference<T> itemHolder = new AtomicReference<>();
		this.toMono().subscribe(item -> {
			itemHolder.set(item);
			latch.countDown();
		});

		long millis = timeout.toMillis();
		if (millis < 0) {
			Try.runOrRethrow(latch::await);
		}
		else {
			boolean normally = Try.supplyOrRethrow(() -> latch.await(millis, TimeUnit.MILLISECONDS));
			if (!normally) {
				throw new TimeoutException(String.format("The observable not completed in %sms", millis));
			}
		}
		return itemHolder.get();
	}

	/**
	 * Returns observable, which will be produce another item, if this observable not produces in given duration.
	 *
	 * @param timeout to wait. Note: ceil to milliseconds.
	 *
	 * @return new observable
	 */
	default MonoObservable<T> getOnTimout(Duration timeout, Supplier<T> factory) {
		return new GetOnTimeoutObservable(this, timeout, factory);
	}

	// transform functions --------------------------------------

	/**
	 * Return observable, which will subscribe to this and transform every object according to given transformer.
	 *
	 * @param mapper for transform objects
	 * @param <R>    type of result object
	 *
	 * @return new observable
	 */
	default <R> TransformObservable<T, R> map(Function<? super T, ? extends R> mapper) {
		return new MapperObservable<>(this, mapper);
	}

	// filtering functions --------------------------------------

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
	 * Return observable, which will subscribe to this observable
	 * and unsubscribe, when predicate result will be negative.
	 *
	 * @param predicate for testing objects
	 *
	 * @return new observable
	 */
	default Observable<T> takeWhile(Predicate<? super T> predicate) {
		return new TakeWhileObservable<>(this, predicate);
	}

	/**
	 * Return observable, which will subscribe to this and skip n objects.
	 *
	 * @param count for skipped objects
	 *
	 * @return new observable
	 */
	default Observable<T> skip(long count) {
		return new SkipObservable<>(this, count);
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

	// collect functions --------------------------------------

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

	// other functions --------------------------------------

	/**
	 * Convert this observable to {@link MonoObservable}
	 *
	 * @return new observable
	 */
	default MonoObservable<T> toMono() {
		return new OnceObservable<>(this);
	}

	/**
	 * Return observable, which will subscribe to this and do given action on every consumed object.
	 *
	 * @param action to perform over objects
	 *
	 * @return new observable
	 */
	default Observable<T> peek(Consumer<? super T> action) {
		return new PeekObservable<>(this, action);
	}

	// factory functions --------------------------------------

	/**
	 * Return empty observable, which will be immediately completed.
	 *
	 * @param <T> type of observable
	 *
	 * @return observable
	 */
	static <T> Observable<T> empty() {
		SimplePublisher<T> publisher = new SimplePublisher<>();
		publisher.complete();
		return publisher.asObservable();
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
		for (T value : values) {
			subject.publish(value);
		}
		subject.complete();
		return subject.asObservable();
	}

	// multiple observable functions --------------------------------------

	/**
	 * Return observable, which will be subscribe to given observables and publish objects from each of them.
	 *
	 * @param observables to subscribe
	 * @param <T>         type of object
	 *
	 * @return observable
	 */
	@SafeVarargs
	static <T> Observable<T> concat(Observable<? extends T>... observables) {
		List<Observable<? extends T>> list = new ArrayList<>(Arrays.asList(observables));
		return new ConcatObservable<>(list);
	}

	/**
	 * Return observable, which will be subscribe to given observables and publish objects from each of them.
	 *
	 * @param observables to subscribe
	 *
	 * @return observable
	 */
	@SuppressWarnings("all")
	static Observable<Object> concatRaw(Observable<?>... observables) {
		List<Observable<? extends Object>> list = (List) Arrays.asList(observables);
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
	static <T> Observable<T> concat(Iterable<Observable<? extends T>> observables) {
		List<Observable<? extends T>> list = new ArrayList<>();
		for (Observable<? extends T> observable : observables) {
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
	static <T> Observable<List<T>> zip(Observable<? extends T>... observables) {
		List<Observable<? extends T>> list = new ArrayList<>(Arrays.asList(observables));
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
	static <T> Observable<List<T>> zip(Iterable<? extends Observable<? extends T>> observables) {
		List<Observable<? extends T>> list = new ArrayList<>();
		for (Observable<? extends T> observable : observables) {
			list.add(observable);
		}
		return new ZipObservable<>(list);
	}

	/**
	 * Return observable, which will be subscribe to given observables.
	 * It will ba wait for objects from every observable and then combine them to as tuple and publish.
	 *
	 * @param observable1 to subscribe
	 * @param observable2 to subscribe
	 * @param <A>         type of first object
	 * @param <B>         type of second object
	 *
	 * @return observable
	 */
	@SuppressWarnings("all")
	static <A, B> Observable<Tuple<A, B>> zip(Observable<? extends A> observable1,
											  Observable<? extends B> observable2) {
		ZipObservable<List<?>> zipObservable = new ZipObservable<>((List) List.of(observable1, observable2));
		return zipObservable.map(list -> new Tuple<>((A) list.get(0), (B) list.get(1)));
	}
}
