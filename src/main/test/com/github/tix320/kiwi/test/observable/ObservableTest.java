package com.github.tix320.kiwi.test.observable;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.Subscription;
import com.github.tix320.kiwi.api.observable.subject.Subject;
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

		empty.one().subscribe(integer -> {
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

		of.one().subscribe(actual::add);

		assertEquals(expected, actual);
	}

	@Test
	void concatTest() {

		List<Integer> expected = Arrays.asList(10, 20, 25);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> observable1 = Observable.of(10);

		Observable<Integer> observable2 = Observable.of(20);

		Subject<Integer> subject = Subject.simple();

		Observable<Integer> observable3 = subject.asObservable();

		Subscription subscription = Observable.concat(observable1, observable2, observable3).subscribe(actual::add);

		subject.next(25);

		subscription.unsubscribe();

		subject.next(50);

		assertEquals(expected, actual);
	}

	@Test
	void concatWithDecoratorTest() {

		List<Integer> expected = Arrays.asList(10, 20, 30, 40, 50, 60, 70);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> observable1 = Observable.of(10, 20, 30);

		Observable<Integer> observable2 = Observable.of(40, 50, 60);

		Subject<Integer> subject = Subject.simple();

		Observable<Integer> observable3 = subject.asObservable();

		Observable.concat(observable1, observable2, observable3).take(7).subscribe(actual::add);

		subject.next(new Integer[]{
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

		Subject<Integer> subject = Subject.simple();

		Observable<Integer> observable3 = subject.asObservable();

		Observable.concat(observable1, observable2, observable3)
				.join(integer -> integer + "", ",")
				.subscribe(actual::set);

		subject.next(25);

		subject.next(50);

		subject.complete();

		assertEquals("10,20,25,50", actual.get());
	}

	@Test
	void combineTest() {
		List<Integer> expected = Arrays.asList(10, 20, -1, 30, 50, -1);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> observable1 = Observable.of(10, 30, 40);

		Observable<Integer> observable2 = Observable.of(20, 50);

		Observable.combine(observable1, observable2).subscribe(integers -> {
			actual.addAll(integers);
			actual.add(-1);
		});

		assertEquals(expected, actual);
	}

	@Test
	void mapTest() {

		List<String> expected = Arrays.asList("10lol", "20lol", "20wtf", "25lol");
		List<String> actual = new ArrayList<>();

		Subject<Integer> subject = Subject.simple();

		Observable<Integer> observable = subject.asObservable();

		Subscription subscription = observable.map(integer -> integer + "lol").subscribe(actual::add);

		subject.next(10);
		observable.map(integer -> integer + "wtf").one().subscribe(actual::add);

		subject.next(20);
		subject.next(25);

		subscription.unsubscribe();

		subject.next(50);

		assertEquals(expected, actual);
	}

	@Test
	void untilTest() {

		List<Integer> expected = Arrays.asList(10, 20, 25);
		List<Integer> actual = new ArrayList<>();

		Subject<Integer> subject = Subject.simple();

		Observable<Integer> observable = subject.asObservable();

		Subject<Object> untilSubject = Subject.simple();

		Observable<?> untilObservable = untilSubject.asObservable();

		observable.takeUntil(untilObservable).subscribe(actual::add);

		subject.next(10);
		subject.next(20);
		subject.next(25);

		untilSubject.complete();

		subject.next(50);
		subject.next(60);

		assertEquals(expected, actual);
	}

	@Test
	void blockTest() {
		List<Integer> expected = Arrays.asList(20, 40);
		List<Integer> actual = new ArrayList<>();

		Subject<Integer> subject = Subject.simple();
		Observable<Integer> observable = subject.asObservable();

		CompletableFuture.runAsync(() -> {
			try {
				TimeUnit.SECONDS.sleep(1);
				subject.next(10);
				subject.next(20);
				subject.next(25);
			}
			catch (InterruptedException e) {
				throw new IllegalStateException();
			}
		}).exceptionally(throwable -> {
			throwable.printStackTrace();
			return null;
		});

		assertTimeout(Duration.ofSeconds(5), () -> {
			observable.take(2).block().map(integer -> integer * 2).subscribe(actual::add);
		});

		assertEquals(expected, actual);
	}

	@Test
	void blockBeforeTakeTest() {
		List<Integer> expected = Arrays.asList(20, 40);
		List<Integer> actual = new ArrayList<>();

		Subject<Integer> subject = Subject.simple();
		Observable<Integer> observable = subject.asObservable();

		CompletableFuture.runAsync(() -> {
			try {
				TimeUnit.SECONDS.sleep(1);
				subject.next(10);
				subject.next(20);
				subject.next(25);
			}
			catch (InterruptedException e) {
				throw new IllegalStateException();
			}
		}).exceptionally(throwable -> {
			throwable.printStackTrace();
			return null;
		});

		assertTimeout(Duration.ofSeconds(5), () -> {
			observable.block().take(2).map(integer -> integer * 2).subscribe(actual::add);
		});

		assertEquals(expected, actual);
	}

	@Test
	void blockWithCompletedTest() {
		List<Integer> expected = Arrays.asList(20, 40, 50);
		List<Integer> actual = new ArrayList<>();

		Subject<Integer> subject = Subject.simple();
		Observable<Integer> observable = subject.asObservable();

		CompletableFuture.runAsync(() -> {
			try {
				TimeUnit.SECONDS.sleep(1);
				subject.next(10);
				subject.next(20);
				subject.next(25);
				subject.complete();
			}
			catch (InterruptedException e) {
				throw new IllegalStateException();
			}
		}).exceptionally(throwable -> {
			throwable.printStackTrace();
			return null;
		});

		assertTimeout(Duration.ofSeconds(5), () -> {
			observable.block().map(integer -> integer * 2).subscribe(actual::add);
		});

		assertEquals(expected, actual);
	}
}

