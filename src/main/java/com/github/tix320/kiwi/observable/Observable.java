package com.github.tix320.kiwi.observable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.github.tix320.kiwi.observable.transform.multiple.internal.CombineLatestObservable;
import com.github.tix320.kiwi.observable.transform.multiple.internal.MergeObservable;
import com.github.tix320.kiwi.observable.transform.multiple.internal.ZipObservable;
import com.github.tix320.kiwi.observable.transform.single.collect.internal.JoinObservable;
import com.github.tix320.kiwi.observable.transform.single.collect.internal.ToListObservable;
import com.github.tix320.kiwi.observable.transform.single.collect.internal.ToMapObservable;
import com.github.tix320.kiwi.observable.transform.single.operator.internal.*;
import com.github.tix320.kiwi.observable.transform.single.timeout.internal.GetOnTimeoutObservable;
import com.github.tix320.kiwi.publisher.MonoPublisher;
import com.github.tix320.kiwi.publisher.SimplePublisher;
import com.github.tix320.kiwi.publisher.UnlimitBufferedPublisher;
import com.github.tix320.skimp.api.collection.Tuple;
import com.github.tix320.skimp.api.exception.ThreadInterruptedException;

/**
 * @param <T> type of data.
 * @author Tigran Sargsyan on 21-Feb-19
 */
public abstract class Observable<T> implements ObservableCandidate<T> {


	/**
	 * Subscribe to observable and consume events asynchronously.
	 *
	 * @param subscriber for subscribing
	 */
	public abstract void subscribe(Subscriber<? super T> subscriber);

	/**
	 * Subscribe to observable and consume items asynchronously.
	 *
	 * @param consumer for processing items
	 */
	public final void subscribe(Consumer<? super T> consumer) {
		subscribe(new FlexibleSubscriber<>() {
			@Override
			public void onNext(T item) {
				consumer.accept(item);
			}
		});
	}

	/**
	 * Subscribe to observable and consume items asynchronously.
	 *
	 * @param consumer for processing items
	 */
	public final void subscribe(Consumer<Subscription> onSubscribe, Consumer<? super T> consumer) {
		subscribe(new FlexibleSubscriber<>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				onSubscribe.accept(subscription);
			}

			@Override
			public void onNext(T item) {
				consumer.accept(item);
			}
		});
	}

	/**
	 * Subscribe to observable and consume items asynchronously.
	 *
	 * @param consumer for processing items
	 */
	public final void subscribe(Consumer<Subscription> onSubscribe, Consumer<? super T> consumer,
								Consumer<Completion> onComplete) {
		subscribe(new Subscriber<T>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				onSubscribe.accept(subscription);
			}

			@Override
			public void onNext(T item) {
				consumer.accept(item);
			}

			@Override
			public void onComplete(Completion completion) {
				onComplete.accept(completion);
			}
		});
	}

	/**
	 * Subscribe to observable and handle every item, consumer must return boolean value,
	 * which indicates that need more elements or not.
	 *
	 * @param consumer for processing items
	 */
	public final void conditionalSubscribe(ConditionalConsumer<T> consumer) {
		subscribe(new FlexibleSubscriber<>() {
			@Override
			public void onNext(T item) {
				boolean needMore = consumer.accept(item);
				if (!needMore) {
					subscription().cancel();
				}
			}
		});
	}

	/**
	 * Subscribe to observable completeness.
	 *
	 * @param onComplete for processing completeness
	 */
	public final void subscribeOnComplete(Consumer<Completion> onComplete) {
		subscribe(new FlexibleSubscriber<>() {
			@Override
			public void onComplete(Completion completion) {
				onComplete.accept(completion);
			}
		});
	}

	@Override
	public final Observable<T> asObservable() {
		return this;
	}

	//region await functions

	/**
	 * Blocks current thread until this observable will be completed.
	 *
	 * @throws ThreadInterruptedException when thread was interrupted
	 * @deprecated use of this method may cause some thread problems until deadlock, for example in the case of blocking while holding the lock/monitor. Use {{@link #await(Duration)} (Duration)}} instead.
	 */
	@Deprecated
	public final void await() {
		await(Duration.ofMillis(-1));
	}

	/**
	 * Blocks current thread for given until this observable will be completed.
	 *
	 * @param timeout timeout to wait. Note: ceil to milliseconds.
	 * @throws TimeoutException           when observable not completed in given time.
	 * @throws ThreadInterruptedException when thread was interrupted
	 */
	public final void await(Duration timeout) {
		CountDownLatch latch = new CountDownLatch(1);
		AtomicBoolean isTimout = new AtomicBoolean(false);

		long millis = timeout.toMillis();

		Observable.this.subscribeOnComplete(completionType -> {
			isTimout.compareAndSet(false, true);
			latch.countDown();
		});

		if (millis < 0) {
			try {
				latch.await();
			}
			catch (InterruptedException e) {
				throw new ThreadInterruptedException();
			}
		}
		else {
			boolean normally;
			try {
				normally = latch.await(millis, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e) {
				throw new ThreadInterruptedException();
			}
			if (!normally) {
				boolean changed = isTimout.compareAndSet(false, true);
				if (changed) {
					throw new TimeoutException(String.format("The observable not completed in %sms", millis));
				}
			}
		}
	}

	/**
	 * Blocks current thread until this observable will be published one value and return.
	 *
	 * @throws ThreadInterruptedException when thread was interrupted
	 * @deprecated use of this method may cause some thread problems until deadlock, for example in the case of blocking while holding the lock/monitor. Use {{@link #get(Duration)}} instead.
	 */
	@Deprecated
	public final T get() {
		return get(Duration.ofSeconds(-1));
	}

	/**
	 * Blocks current thread for given timout until this observable will be published one value and return.
	 *
	 * @param timeout timeout to wait. Note: ceil to milliseconds.
	 * @throws TimeoutException           when observable not completed in given time.
	 * @throws ThreadInterruptedException when thread was interrupted
	 */
	public final T get(Duration timeout) {
		AtomicReference<T> itemHolder = new AtomicReference<>();
		waitAndApply(timeout, itemHolder::set);
		return itemHolder.get();
	}

	/**
	 * Blocks current thread for given timout until this observable will be published one value and apply given consumer to that item.
	 *
	 * @param timeout timeout to wait. Note: ceil to milliseconds.
	 * @throws TimeoutException           when observable not completed in given time.
	 * @throws ThreadInterruptedException when thread was interrupted
	 */
	public final void waitAndApply(Duration timeout, Consumer<T> consumer) {
		CountDownLatch latch = new CountDownLatch(1);

		long millis = timeout.toMillis();

		AtomicBoolean isTimout = new AtomicBoolean(false);
		this.toMono().subscribe(item -> {
			boolean changed = isTimout.compareAndSet(false, true);
			if (changed) {
				consumer.accept(item);
			}
			latch.countDown();
		});

		if (millis < 0) {
			try {
				latch.await();
			}
			catch (InterruptedException e) {
				throw new ThreadInterruptedException();
			}
		}
		else {
			boolean normally;
			try {
				normally = latch.await(millis, TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e) {
				throw new ThreadInterruptedException();
			}
			if (!normally) {
				boolean changed = isTimout.compareAndSet(false, true);
				if (changed) {
					throw new TimeoutException(String.format("The observable not completed in %sms", millis));
				}
			}
		}
	}

	/**
	 * Returns observable, which will be produce another item, if this observable not produces in given duration.
	 *
	 * @param timeout to wait. Note: ceil to milliseconds.
	 * @return new observable
	 */
	public final MonoObservable<T> getOnTimout(Duration timeout, Supplier<T> factory) {
		return new GetOnTimeoutObservable<>(this, timeout, factory);
	}
	//endregion

	//region transform functions

	/**
	 * Return observable, which will subscribe to this and transform every object according to given transformer.
	 *
	 * @param mapper for transform objects
	 * @param <R>    type of result object
	 * @return new observable
	 */
	public final <R> Observable<R> map(Function<? super T, ? extends R> mapper) {
		return new MapperObservable<>(this, mapper);
	}

	// filtering functions --------------------------------------

	/**
	 * Return observable, which will subscribe to this and receive n objects, after that unsubscribe.
	 *
	 * @param count for wanted objects
	 * @return new observable
	 */
	public final Observable<T> take(long count) {
		return new CountingObservable<>(this, count);
	}

	/**
	 * Return observable, which will subscribe to given observable
	 * and unsubscribe from this observable, when given will be completed.
	 *
	 * @param observable to subscribe
	 * @return new observable
	 */
	public final Observable<T> takeUntil(Observable<?> observable) {
		return new UntilObservable<>(this, observable);
	}

	/**
	 * Return observable, which will subscribe to this observable
	 * and unsubscribe, when predicate result will be negative.
	 *
	 * @param predicate for testing objects
	 * @return new observable
	 */
	public final Observable<T> takeWhile(Predicate<? super T> predicate) {
		return new TakeWhileObservable<>(this, predicate);
	}

	/**
	 * Return observable, which will subscribe to this and skip n objects.
	 *
	 * @param count for skipped objects
	 * @return new observable
	 */
	public final Observable<T> skip(long count) {
		return new SkipObservable<>(this, count);
	}

	/**
	 * Return observable, which will subscribe to this and set filter to objects according to given filter.
	 *
	 * @param filter for filtering objects
	 * @return new observable
	 */
	public final Observable<T> filter(Predicate<? super T> filter) {
		return new FilterObservable<>(this, filter);
	}
	//endregion

	//region collect functions

	/**
	 * Return observable, which will subscribe to this and wait until it will be completed,
	 * and collect received values to map according to given factories.
	 *
	 * @param keyMapper   for extracting map key from objects
	 * @param valueMapper for extracting map value from objects
	 * @param <K>         type of map key
	 * @param <V>         type of map value
	 * @return new observable
	 */
	public final <K, V> Observable<Map<K, V>> toMap(Function<? super T, ? extends K> keyMapper,
													Function<? super T, ? extends V> valueMapper) {
		return new ToMapObservable<>(this, keyMapper, valueMapper);
	}

	/**
	 * Return observable, which will subscribe to this and wait until it will be completed,
	 * and collect received values to list.
	 *
	 * @return new observable
	 */
	public final Observable<List<T>> toList() {
		return new ToListObservable<>(this);
	}

	/**
	 * Return observable, which will subscribe to this and wait until it will be completed,
	 * and join received values string representations.
	 *
	 * @param toString  to transform object to string
	 * @param delimiter to separate transformed strings
	 * @return new observable
	 */
	public final Observable<String> join(Function<? super T, ? extends String> toString, String delimiter) {
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
	 * @return new observable
	 */
	public final Observable<String> join(Function<? super T, ? extends String> toString, String delimiter,
										 String prefix, String suffix) {
		return new JoinObservable<>(this, toString, delimiter, prefix, suffix);
	}
	//endregion

	//region other

	/**
	 * Convert this observable to {@link MonoObservable} or return @this if it is already mono.
	 *
	 * @return new observable
	 */
	public final MonoObservable<T> toMono() {
		if (this instanceof MonoObservable<T> monoObservable) {
			return monoObservable;
		}
		return new OnceObservable<>(this);
	}

	/**
	 * Return observable, which will subscribe to this and do given action on every consumed object.
	 *
	 * @param action to perform over objects
	 * @return new observable
	 */
	public final Observable<T> peek(Consumer<? super T> action) {
		return new PeekObservable<>(this, action);
	}

	// factory functions --------------------------------------

	/**
	 * Return empty observable, which will be immediately completed.
	 *
	 * @param <T> type of observable
	 * @return observable
	 */
	public static <T> Observable<T> empty() {
		SimplePublisher<T> publisher = new SimplePublisher<>();
		publisher.complete();
		return publisher.asObservable();
	}

	/**
	 * Return observable, which will be produce given object and then immediately completed.
	 *
	 * @param value to publish
	 * @param <T>   type of object
	 * @return observable
	 */
	public static <T> MonoObservable<T> of(T value) {
		MonoPublisher<T> monoPublisher = new MonoPublisher<>();
		monoPublisher.publish(value);
		return monoPublisher.asObservable();
	}

	/**
	 * Return observable, which will be produce items from given iterable and then immediately completed.
	 *
	 * @param iterable to publish
	 * @param <T>      type of objects
	 * @return observable
	 */
	public static <T> Observable<T> of(Iterable<T> iterable) {
		UnlimitBufferedPublisher<T> publisher = new UnlimitBufferedPublisher<>(iterable);
		publisher.complete();
		return publisher.asObservable();
	}

	/**
	 * Return observable, which will be produce given objects and then immediately completed.
	 *
	 * @param values to publish
	 * @param <T>    type of object
	 * @return observable
	 */
	@SafeVarargs
	public static <T> Observable<T> of(T... values) {
		UnlimitBufferedPublisher<T> publisher = new UnlimitBufferedPublisher<>(Arrays.asList(values));
		publisher.complete();
		return publisher.asObservable();
	}
	//endregion

	//region combining functions

	/**
	 * Return observable, which will be subscribe to given observables and publish objects from each of them.
	 *
	 * @param observables to subscribe
	 * @param <T>         type of object
	 * @return observable
	 */
	@SafeVarargs
	public static <T> Observable<T> merge(Observable<? extends T>... observables) {
		List<Observable<? extends T>> list = Arrays.asList(observables);
		return new MergeObservable<>(list);
	}

	/**
	 * Return observable, which will be subscribe to given observables and publish objects from each of them.
	 *
	 * @param observables to subscribe
	 * @return observable
	 */
	@SuppressWarnings("all")
	public static Observable<Object> mergeRaw(Observable<?>... observables) {
		List<Observable<? extends Object>> list = (List) Arrays.asList(observables);
		return new MergeObservable<>(list);
	}

	/**
	 * Return observable, which will be subscribe to given observables and publish objects from each of them.
	 *
	 * @param observables to subscribe
	 * @param <T>         type of object
	 * @return observable
	 */
	public static <T> Observable<T> merge(Iterable<Observable<? extends T>> observables) {
		List<Observable<? extends T>> list = new ArrayList<>();
		for (Observable<? extends T> observable : observables) {
			list.add(observable);
		}
		return new MergeObservable<>(list);
	}

	/**
	 * Return observable, which will be subscribe to given observables.
	 * It will ba wait for objects from every observable and then combine them to list and publish.
	 *
	 * @param observables to subscribe
	 * @param <T>         type of object
	 * @return observable
	 */
	@SafeVarargs
	public static <T> Observable<List<T>> zip(Observable<? extends T>... observables) {
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
	 * @return observable
	 */
	public static <T> Observable<List<T>> zip(Iterable<? extends Observable<? extends T>> observables) {
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
	 * @return observable
	 */
	@SuppressWarnings("all")
	public static <A, B> Observable<Tuple<A, B>> zip(Observable<? extends A> observable1,
													 Observable<? extends B> observable2) {
		ZipObservable<List<?>> zipObservable = new ZipObservable<>((List) List.of(observable1, observable2));
		return zipObservable.map(list -> new Tuple<>((A) list.get(0), (B) list.get(1)));
	}


	/**
	 * Return observable, which will be subscribe to given observables.
	 * It will be wait for every publish from given observables and combine latest items of every observable as tuple and publish.
	 *
	 * @param observable1 to subscribe
	 * @param observable2 to subscribe
	 * @param <A>         type of first object
	 * @param <B>         type of second object
	 * @return observable
	 */
	@SuppressWarnings("all")
	public static <A, B> Observable<Tuple<A, B>> combineLatest(Observable<? extends A> observable1,
															   Observable<? extends B> observable2) {
		CombineLatestObservable<List> combineLatestObservable = new CombineLatestObservable(
				List.of(observable1, observable2));
		return combineLatestObservable.map(list -> new Tuple<>((A) list.get(0), (B) list.get(1)));
	}
	//endregion
}
