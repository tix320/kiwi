package com.github.tix320.kiwi.reactive;

import com.github.tix320.kiwi.extension.AsyncExceptionCheckerExtension;
import com.github.tix320.kiwi.extension.KiwiSchedulerRefreshExtension;
import com.github.tix320.kiwi.observable.Completion;
import com.github.tix320.kiwi.observable.FlexibleSubscriber;
import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.scheduler.KiwiSchedulerHolder;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.utils.SchedulerUtils;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
@ExtendWith({AsyncExceptionCheckerExtension.class, KiwiSchedulerRefreshExtension.class})
public class ObservableTest {

	@Test
	public void emptyTest() throws InterruptedException {
		Observable<Integer> empty = Observable.empty();

		empty.subscribe(integer -> {
			throw new IllegalStateException();
		});

		empty.toMono().subscribe(integer -> {
			throw new IllegalStateException();
		});

		AtomicReference<Map<Integer, Integer>> actual = new AtomicReference<>(null);
		empty.toMap(integer -> integer, integer -> integer).subscribe(actual::set);

		SchedulerUtils.awaitTermination();

		assertEquals(Map.of(), actual.get());
	}

	@Test
	public void ofTest() throws InterruptedException {
		List<Integer> expected = Arrays.asList(32, 32, 32);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Observable<Integer> of = Observable.of(32);

		of.subscribe(actual::add);

		of.subscribe(actual::add);

		of.toMono().subscribe(actual::add);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	// @Test
	// public void concatTest() throws InterruptedException {
	// 	System.out.println(Thread.currentThread().getStackTrace()[1]);
	// 	Set<Integer> expected = Set.of(10, 20, 25, 50);
	// 	Set<Integer> actual = new ConcurrentSkipListSet<>();
	//
	// 	Observable<Integer> observable1 = Observable.of(10);
	//
	// 	Observable<Integer> observable2 = Observable.of(20);
	//
	// 	Publisher<Integer> publisher = Publisher.simple();
	//
	// 	Observable<Integer> observable3 = publisher.asObservable();
	//
	// 	Observable.concat(observable1, observable2, observable3).conditionalSubscribe(actual::add);
	//
	// 	publisher.publish(25);
	//
	// 	publisher.publish(50);
	//
	// 	SchedulerUtils.awaitTermination();
	//
	// 	assertEquals(expected, actual);
	// }

	// @Test
	// public void concatWithDecoratorTest() throws InterruptedException {
	// 	System.out.println(Thread.currentThread().getStackTrace()[1]);
	// 	Set<Integer> canBe = Set.of(10, 20, 30, 40, 50, 60, 70, 80);
	// 	Set<Integer> actual = Collections.synchronizedSet(new HashSet<>());
	//
	// 	Observable<Integer> observable1 = Observable.of(10, 20, 30);
	//
	// 	Observable<Integer> observable2 = Observable.of(40, 50, 60);
	//
	// 	Publisher<Integer> publisher = Publisher.simple();
	//
	// 	Observable<Integer> observable3 = publisher.asObservable();
	//
	// 	Observable.concat(observable1, observable2, observable3).take(7).subscribe(actual::add);
	//
	// 	publisher.publish(70);
	// 	publisher.publish(80);
	//
	// 	SchedulerUtils.awaitTermination();
	//
	// 	assertEquals(7, actual.size());
	// 	for (Integer integer : actual) {
	// 		assertTrue(canBe.contains(integer));
	// 	}
	// }
	//
	// @Test
	// public void concatOnCompleteTest() throws InterruptedException {
	// 	System.out.println(Thread.currentThread().getStackTrace()[1]);
	// 	AtomicReference<String> actual = new AtomicReference<>("");
	//
	// 	Observable<Integer> observable1 = Observable.of(10);
	//
	// 	Observable<Integer> observable2 = Observable.of(20);
	//
	// 	Publisher<Integer> publisher = Publisher.simple();
	//
	// 	Observable<Integer> observable3 = publisher.asObservable();
	//
	// 	Observable.concat(observable1, observable2, observable3)
	// 			.join(integer -> integer + "", ",")
	// 			.subscribe(actual::set);
	//
	// 	publisher.publish(25);
	//
	// 	publisher.publish(50);
	//
	// 	publisher.complete();
	//
	// 	SchedulerUtils.awaitTermination();
	//
	// 	assertEquals(Set.of("10", "20", "25", "50"), new HashSet<>(Arrays.asList(actual.get().split(","))));
	// }

	@Test
	public void mapTest() throws InterruptedException {
		List<String> expected1 = List.of("10lol", "20lol", "25lol");
		List<String> expected2 = List.of("20wtf");

		List<String> actual1 = new CopyOnWriteArrayList<>();
		List<String> actual2 = new CopyOnWriteArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable = publisher.asObservable();

		observable.map(integer -> integer + "lol")
			.takeWhile(item -> !item.equals("50lol"))
			.subscribe(actual1::add);

		publisher.publish(10);
		observable.map(integer -> integer + "wtf").toMono().subscribe(actual2::add);

		publisher.publish(20);
		publisher.publish(25);

		publisher.publish(50);

		SchedulerUtils.awaitTermination();

		assertEquals(expected1, actual1);
		assertEquals(expected2, actual2);
	}

	@Test
	public void untilTest() throws InterruptedException {
		List<Integer> expected = Arrays.asList(10, 20, 25);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable = publisher.asObservable();

		Publisher<Object> untilPublisher = Publisher.simple();

		Observable<?> untilObservable = untilPublisher.asObservable();

		observable.takeUntil(untilObservable).subscribe(actual::add);

		publisher.publish(10);
		publisher.publish(20);
		publisher.publish(25);

		untilPublisher.complete();

		publisher.publish(50);
		publisher.publish(60);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void awaitTest() {
		List<Integer> expected = Arrays.asList(20, 40);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		KiwiSchedulerHolder.get().schedule(() -> {
			SchedulerUtils.sleepFor(Duration.ofMillis(100));
			publisher.publish(10);
			publisher.publish(20);
			publisher.publish(25);
		});

		observable.take(2).map(integer -> integer * 2).peek(actual::add).await(Duration.ofSeconds(5));

		assertEquals(expected, actual);
	}

	@Test
	public void awaitBeforeTakeTest() {
		List<Integer> expected = Arrays.asList(20, 40);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();
		KiwiSchedulerHolder.get().schedule(() -> {
			SchedulerUtils.sleepFor(Duration.ofMillis(100));
			publisher.publish(10);
			publisher.publish(20);
			publisher.publish(25);
		});

		observable.take(2).map(integer -> integer * 2).peek(actual::add).await(Duration.ofSeconds(5));

		assertEquals(expected, actual);
	}

	@Test
	public void awaitWithCompletedTest() {
		List<Integer> expected = Arrays.asList(20, 40, 50);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		KiwiSchedulerHolder.get().schedule(() -> {
			SchedulerUtils.sleepFor(Duration.ofMillis(100));
			publisher.publish(10);
			publisher.publish(20);
			publisher.publish(25);
			publisher.complete();
		});

		observable.map(integer -> integer * 2).peek(actual::add).await(Duration.ofSeconds(5));

		assertEquals(expected, actual);
	}

	@Test
	public void blockWithCompleteTest() {
		List<Integer> expected = Arrays.asList(10, 20, 25);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		KiwiSchedulerHolder.get().schedule(() -> {
			SchedulerUtils.sleepFor(Duration.ofMillis(100));
			publisher.publish(10);
			publisher.publish(20);
			publisher.publish(25);
			publisher.complete();
		});

		observable.map(actual::add).await(Duration.ofSeconds(5));

		assertEquals(expected, actual);
	}

	@Test
	public void blockWithZipTest() {
		List<Integer> expected = Arrays.asList(10, 20);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.simple();
		Observable<Integer> observable1 = publisher1.asObservable();
		Observable<Integer> observable2 = publisher2.asObservable();

		KiwiSchedulerHolder.get().schedule(() -> {
			SchedulerUtils.sleepFor(Duration.ofMillis(100));
			publisher1.publish(10);
			SchedulerUtils.sleepFor(Duration.ofMillis(100));
			publisher2.publish(20);
			publisher1.complete();
			publisher2.complete();
		});

		Observable.zip(observable1, observable2).peek(integerIntegerTuple -> {
			actual.add(integerIntegerTuple.first());
			actual.add(integerIntegerTuple.second());
		}).await(Duration.ofSeconds(5));

		assertEquals(expected, actual);
	}

	@Test
	public void getTest() {
		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(2);
		CompletableFuture.runAsync(() -> {
			SchedulerUtils.sleepFor(Duration.ofMillis(50));
			publisher.publish(3);
		});

		Integer number = observable.map(integer -> integer + 5).get(Duration.ofSeconds(5));
		assertEquals(8, number);
	}

	@Test
	public void getWithBufferedTest() {
		Publisher<Integer> publisher = Publisher.buffered(1);
		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(2);
		KiwiSchedulerHolder.get().schedule(() -> {
			SchedulerUtils.sleepFor(Duration.ofMillis(100));
			publisher.publish(3);
		});
		Integer number = observable.map(integer -> integer + 5).get(Duration.ofSeconds(5));

		assertEquals(7, number);
	}

	@Test
	public void onCompleteOnUnsubscribe() throws InterruptedException {
		List<Integer> expected = Arrays.asList(1, 2, 15);
		List<Integer> actual = new CopyOnWriteArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable = publisher.asObservable();

		observable.subscribe(new FlexibleSubscriber<>() {
			@Override
			public void onNext(Integer item) {
				actual.add(item);
				if (item.equals(2)) {
					subscription().cancel();
				}
			}

			@Override
			public void onComplete(Completion completion) {
				actual.add(15);
			}
		});

		publisher.publish(1);
		publisher.publish(2);

		publisher.publish(3);

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

	@Test
	public void doubleUnsubscribe() throws InterruptedException {
		List<Integer> expected = Arrays.asList(1, 2, 15);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable = publisher.asObservable();

		FlexibleSubscriber<Integer> subscriber = new FlexibleSubscriber<>() {

			@Override
			public void onNext(Integer item) {
				actual.add(item);
			}

			@Override
			public void onComplete(Completion completion) {
				actual.add(15);
			}
		};
		observable.subscribe(subscriber);

		publisher.publish(1);
		publisher.publish(2);
		SchedulerUtils.sleepFor(Duration.ofMillis(100));
		subscriber.subscription().cancel();
		publisher.publish(3);
		SchedulerUtils.sleepFor(Duration.ofMillis(100));
		subscriber.subscription().cancel();

		SchedulerUtils.awaitTermination();

		assertEquals(expected, actual);
	}

}
