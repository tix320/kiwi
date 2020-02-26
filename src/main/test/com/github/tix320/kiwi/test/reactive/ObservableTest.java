package com.github.tix320.kiwi.test.reactive;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.api.check.Try;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.WrapperException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

/**
 * @author Tigran Sargsyan on 24-Feb-19
 */
class ObservableTest {

	@Test
	void emptyTest() {
		Observable<Integer> empty = Observable.empty();

		empty.subscribe(integer -> {
			throw new IllegalStateException();
		});

		empty.toMono().subscribe(integer -> {
			throw new IllegalStateException();
		});

		AtomicReference<Map<Integer, Integer>> actual = new AtomicReference<>(null);
		empty.toMap(integer -> integer, integer -> integer).subscribe(actual::set);
		assertEquals(Map.of(), actual.get());
	}

	@Test
	void ofTest() {
		List<Integer> expected = Arrays.asList(32, 32, 32);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> of = Observable.of(32);

		of.subscribe(actual::add);

		of.subscribe(actual::add);

		of.toMono().subscribe(actual::add);

		assertEquals(expected, actual);
	}

	@Test
	void concatTest() {

		List<Integer> expected = Arrays.asList(10, 20, 25);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> observable1 = Observable.of(10);

		Observable<Integer> observable2 = Observable.of(20);

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable3 = publisher.asObservable();

		Subscription subscription = Observable.concat(observable1, observable2, observable3).subscribe(actual::add);

		publisher.publish(25);

		subscription.unsubscribe();

		publisher.publish(50);

		assertEquals(expected, actual);
	}

	@Test
	void concatWithDecoratorTest() {

		List<Integer> expected = Arrays.asList(10, 20, 30, 40, 50, 60, 70);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> observable1 = Observable.of(10, 20, 30);

		Observable<Integer> observable2 = Observable.of(40, 50, 60);

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable3 = publisher.asObservable();

		Observable.concat(observable1, observable2, observable3).take(7).subscribe(actual::add);

		publisher.publish(new Integer[]{
				70,
				80
		});

		assertEquals(expected, actual);
	}

	@Test
	void concatOnCompleteTest() {
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

		assertEquals("10,20,25,50", actual.get());
	}

	@Test
	void zipTest() {
		List<Integer> expected = Arrays.asList(10, 20, -1, 30, 50, -1);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> observable1 = Observable.of(10, 30, 40);

		Observable<Integer> observable2 = Observable.of(20, 50);

		Observable.zip(observable1, observable2).subscribe(integers -> {
			actual.addAll(integers);
			actual.add(-1);
		});

		assertEquals(expected, actual);
	}

	@Test
	void mapTest() {

		List<String> expected = Arrays.asList("10lol", "20lol", "20wtf", "25lol");
		List<String> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable = publisher.asObservable();

		Subscription subscription = observable.map(integer -> integer + "lol").subscribe(actual::add);

		publisher.publish(10);
		observable.map(integer -> integer + "wtf").toMono().subscribe(actual::add);

		publisher.publish(20);
		publisher.publish(25);

		subscription.unsubscribe();

		publisher.publish(50);

		assertEquals(expected, actual);
	}

	@Test
	void untilTest() {

		List<Integer> expected = Arrays.asList(10, 20, 25);
		List<Integer> actual = new ArrayList<>();

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

		assertEquals(expected, actual);
	}

	@Test
	void awaitTest() {
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
			observable.take(2).await().map(integer -> integer * 2).subscribe(actual::add);
		});

		assertEquals(expected, actual);
	}

	@Test
	void awaitBeforeTakeTest() {
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
			observable.await().take(2).map(integer -> integer * 2).subscribe(actual::add);
		});

		assertEquals(expected, actual);
	}

	@Test
	void awaitWithCompletedTest() {
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
			observable.await().map(integer -> integer * 2).subscribe(actual::add);
		});

		assertEquals(expected, actual);
	}

	@Test
	void blockWithCompleteTest() {
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
			observable.map(actual::add).blockUntilComplete();
		});

		assertEquals(expected, actual);
	}

	@Test
	void blockWithZipTest() {
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
			Observable.zip(observable1, observable2).map(actual::addAll).blockUntilComplete();
		});

		assertEquals(expected, actual);
	}

	@Test
	void errorTest() {
		List<Integer> expected = Arrays.asList(1, 2, 15, 3);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		observable.subscribe(actual::add, throwable -> {
			assertEquals("foo", throwable.getMessage());
			return actual.add(15);
		});

		publisher.publish(1);
		publisher.publish(2);

		publisher.publishError(new RuntimeException("foo"));
		publisher.publish(3);

		assertEquals(expected, actual);
	}

	@Test
	void errorTestWithParticular() {
		List<Integer> expected = Arrays.asList(1, 2, 15);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		observable.particularSubscribe(actual::add, throwable -> {
			assertEquals("foo", throwable.getMessage());
			actual.add(15);
			return false;
		});

		publisher.publish(1);
		publisher.publish(2);

		publisher.publishError(new RuntimeException("foo"));
		publisher.publish(3);

		assertEquals(expected, actual);
	}

	@Test
	void errorTestWithThrowing() {
		List<Integer> expected = Arrays.asList(1, 2, 3);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		observable.particularSubscribe(actual::add, throwable -> {
			throw WrapperException.wrap(throwable);
		});

		publisher.publish(1);
		publisher.publish(2);

		publisher.publishError(new RuntimeException("foo"));
		publisher.publish(3);

		assertEquals(expected, actual);
	}

	@Test
	void getTest() {
		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(2);
		CompletableFuture.runAsync(() -> {
			Try.runOrRethrow(() -> Thread.sleep(50));
			publisher.publish(3);
		});
		Integer number = observable.map(integer -> integer + 5).get();

		assertEquals(8, number);
	}

	@Test
	void getWithBufferedTest() {
		Publisher<Integer> publisher = Publisher.buffered(1);
		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(2);
		CompletableFuture.runAsync(() -> {
			Try.runOrRethrow(() -> Thread.sleep(50));
			publisher.publish(3);
		});
		Integer number = observable.map(integer -> integer + 5).get();

		assertEquals(7, number);
	}

	@Test
	void onCompleteOnUnsubscribe() {
		List<Integer> expected = Arrays.asList(1, 2, 15);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable = publisher.asObservable();

		Subscription subscription = observable.subscribe(actual::add, () -> actual.add(15));

		publisher.publish(1);
		publisher.publish(2);
		subscription.unsubscribe();
		publisher.publish(3);

		assertEquals(expected, actual);
	}

	@Test
	void doubleUnsubscribe() {
		List<Integer> expected = Arrays.asList(1, 2, 15);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable = publisher.asObservable();

		Subscription subscription = observable.subscribe(actual::add, () -> actual.add(15));

		publisher.publish(1);
		publisher.publish(2);
		subscription.unsubscribe();
		publisher.publish(3);
		subscription.unsubscribe();

		assertEquals(expected, actual);
	}
}

