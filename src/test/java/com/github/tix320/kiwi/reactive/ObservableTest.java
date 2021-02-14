package com.github.tix320.kiwi.reactive;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
public class ObservableTest {

	@Test
	public void emptyTest() throws InterruptedException {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		Observable<Integer> empty = Observable.empty();

		empty.subscribe(integer -> {
			throw new IllegalStateException();
		});

		empty.toMono().subscribe(integer -> {
			throw new IllegalStateException();
		});

		AtomicReference<Map<Integer, Integer>> actual = new AtomicReference<>(null);
		empty.toMap(integer -> integer, integer -> integer).subscribe(actual::set);

		Thread.sleep(100);

		assertEquals(Map.of(), actual.get());
	}

	@Test
	public void ofTest() throws InterruptedException {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		List<Integer> expected = Arrays.asList(32, 32, 32);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Observable<Integer> of = Observable.of(32);

		of.subscribe(actual::add);

		of.subscribe(actual::add);

		of.toMono().subscribe(actual::add);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	public void concatTest() throws InterruptedException {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		Set<Integer> expected = Set.of(10, 20, 25, 50);
		Set<Integer> actual = new ConcurrentSkipListSet<>();

		Observable<Integer> observable1 = Observable.of(10);

		Observable<Integer> observable2 = Observable.of(20);

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable3 = publisher.asObservable();

		Observable.concat(observable1, observable2, observable3).conditionalSubscribe(actual::add);

		publisher.publish(25);

		publisher.publish(50);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	public void concatWithDecoratorTest() throws InterruptedException {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		Set<Integer> canBe = Set.of(10, 20, 30, 40, 50, 60, 70, 80);
		Set<Integer> actual = Collections.synchronizedSet(new HashSet<>());

		Observable<Integer> observable1 = Observable.of(10, 20, 30);

		Observable<Integer> observable2 = Observable.of(40, 50, 60);

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable3 = publisher.asObservable();

		Observable.concat(observable1, observable2, observable3).take(7).subscribe(actual::add);

		publisher.publish(70);
		publisher.publish(80);

		Thread.sleep(100);

		assertEquals(7, actual.size());
		for (Integer integer : actual) {
			assertTrue(canBe.contains(integer));
		}
	}

	@Test
	public void concatOnCompleteTest() throws InterruptedException {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		AtomicReference<String> actual = new AtomicReference<>("");

		Observable<Integer> observable1 = Observable.of(10);

		Observable<Integer> observable2 = Observable.of(20);

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable3 = publisher.asObservable();

		Observable.concat(observable1, observable2, observable3)
				.join(integer -> integer + "", ",")
				.subscribe(actual::set);

		publisher.publish(25);

		publisher.publish(50);

		publisher.complete();

		Thread.sleep(100);

		assertEquals(Set.of("10", "20", "25", "50"), new HashSet<>(Arrays.asList(actual.get().split(","))));
	}

	@Test
	public void mapTest() throws InterruptedException {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		List<String> expected1 = List.of("10lol", "20lol", "25lol");
		List<String> expected2 = List.of("20wtf");

		List<String> actual1 = new CopyOnWriteArrayList<>();
		List<String> actual2 = new CopyOnWriteArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable = publisher.asObservable();

		observable.map(integer -> integer + "lol").conditionalSubscribe(item -> {
			actual1.add(item);
			return !item.equals("25lol");
		});

		publisher.publish(10);
		observable.map(integer -> integer + "wtf").toMono().subscribe(actual2::add);

		publisher.publish(20);
		publisher.publish(25);

		publisher.publish(50);

		Thread.sleep(100);

		assertEquals(expected1, actual1);
		assertEquals(expected2, actual2);
	}

	@Test
	public void untilTest() throws InterruptedException {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
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

		Thread.sleep(100);

		untilPublisher.complete();

		Thread.sleep(100);

		publisher.publish(50);
		publisher.publish(60);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	public void awaitTest() {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		List<Integer> expected = Arrays.asList(20, 40);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		CompletableFuture.runAsync(() -> {
			try {
				TimeUnit.SECONDS.sleep(1);
				publisher.publish(10);
				publisher.publish(20);
				publisher.publish(25);
			}
			catch (InterruptedException e) {
				throw new IllegalStateException();
			}
		}).exceptionally(throwable -> {
			throwable.printStackTrace();
			return null;
		});

		assertTimeout(Duration.ofSeconds(5), () -> {
			observable.take(2).map(integer -> integer * 2).peek(actual::add).await();
		});

		assertEquals(expected, actual);
	}

	@Test
	public void awaitBeforeTakeTest() {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		List<Integer> expected = Arrays.asList(20, 40);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		CompletableFuture.runAsync(() -> {
			try {
				TimeUnit.SECONDS.sleep(1);
				publisher.publish(10);
				publisher.publish(20);
				publisher.publish(25);
			}
			catch (InterruptedException e) {
				throw new IllegalStateException();
			}
		}).exceptionally(throwable -> {
			throwable.printStackTrace();
			return null;
		});

		assertTimeout(Duration.ofSeconds(5), () -> {
			observable.take(2).map(integer -> integer * 2).peek(actual::add).await();
		});

		assertEquals(expected, actual);
	}

	@Test
	public void awaitWithCompletedTest() {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		List<Integer> expected = Arrays.asList(20, 40, 50);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		CompletableFuture.runAsync(() -> {
			try {
				TimeUnit.SECONDS.sleep(1);
				publisher.publish(10);
				publisher.publish(20);
				publisher.publish(25);
				publisher.complete();
			}
			catch (InterruptedException e) {
				throw new IllegalStateException();
			}
		}).exceptionally(throwable -> {
			throwable.printStackTrace();
			return null;
		});

		assertTimeout(Duration.ofSeconds(5), () -> {
			observable.map(integer -> integer * 2).peek(actual::add).await();
		});

		assertEquals(expected, actual);
	}

	@Test
	public void blockWithCompleteTest() {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		List<Integer> expected = Arrays.asList(10, 20, 25);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		CompletableFuture.runAsync(() -> {
			try {
				TimeUnit.SECONDS.sleep(1);
				publisher.publish(10);
				publisher.publish(20);
				publisher.publish(25);
				publisher.complete();
			}
			catch (InterruptedException e) {
				throw new IllegalStateException();
			}
		}).exceptionally(throwable -> {
			throwable.printStackTrace();
			return null;
		});

		assertTimeout(Duration.ofSeconds(5), () -> {
			observable.map(actual::add).await();
		});

		assertEquals(expected, actual);
	}

	@Test
	public void blockWithZipTest() {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		List<Integer> expected = Arrays.asList(10, 20);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.simple();
		Observable<Integer> observable1 = publisher1.asObservable();
		Observable<Integer> observable2 = publisher2.asObservable();

		CompletableFuture.runAsync(() -> {
			try {
				TimeUnit.SECONDS.sleep(1);
				publisher1.publish(10);
				TimeUnit.SECONDS.sleep(1);
				publisher2.publish(20);
				publisher1.complete();
				publisher2.complete();
			}
			catch (InterruptedException e) {
				throw new IllegalStateException();
			}
		}).exceptionally(throwable -> {
			throwable.printStackTrace();
			return null;
		});

		assertTimeout(Duration.ofSeconds(5), () -> {
			Observable.zip(observable1, observable2).peek(integerIntegerTuple -> {
				actual.add(integerIntegerTuple.first());
				actual.add(integerIntegerTuple.second());
			}).await();
		});

		assertEquals(expected, actual);
	}

	@Test
	public void getTest() throws InterruptedException {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(2);
		CompletableFuture.runAsync(() -> {
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
			publisher.publish(3);
		});
		Integer number = observable.map(integer -> integer + 5).get();

		assertEquals(8, number);
	}

	@Test
	public void getWithBufferedTest() throws InterruptedException {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		Publisher<Integer> publisher = Publisher.buffered(1);
		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(2);
		CompletableFuture.runAsync(() -> {
			try {
				Thread.sleep(50);
			}
			catch (InterruptedException e) {
				throw new IllegalStateException(e);
			}
			publisher.publish(3);
		});
		Integer number = observable.map(integer -> integer + 5).get();

		assertEquals(7, number);
	}

	@Test
	public void onCompleteOnUnsubscribe() throws InterruptedException {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		List<Integer> expected = Arrays.asList(1, 2, 15);
		List<Integer> actual = new CopyOnWriteArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable = publisher.asObservable();

		observable.subscribe(Subscriber.<Integer>builder().onPublishConditional(object -> {
			actual.add(object);
			return !object.equals(2);
		}).onComplete((completionType) -> actual.add(15)));

		publisher.publish(1);
		publisher.publish(2);

		publisher.publish(3);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	public void doubleUnsubscribe() throws InterruptedException {
		System.out.println(Thread.currentThread().getStackTrace()[1]);
		List<Integer> expected = Arrays.asList(1, 2, 15);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable = publisher.asObservable();

		AtomicReference<Subscription> subscriptionHolder = new AtomicReference<>();
		observable.subscribe(Subscriber.<Integer>builder().onSubscribe(subscriptionHolder::set)
				.onPublish(actual::add)
				.onComplete((completionType) -> actual.add(15)));

		publisher.publish(1);
		publisher.publish(2);
		subscriptionHolder.get().unsubscribe();
		publisher.publish(3);
		subscriptionHolder.get().unsubscribe();

		Thread.sleep(100);

		assertEquals(expected, actual);
	}
}

