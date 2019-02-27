package io.titix.kiwi.rx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author tix32 on 24-Feb-19
 */
class ObservableTest {

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

		Subject<Integer> subject = Subject.single();

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

		Subject<Integer> subject = Subject.single();

		Observable<Integer> observable3 = subject.asObservable();

		Observable.concat(observable1, observable2, observable3)
				.take(7)
				.subscribe(actual::add);

		subject.next(new Integer[]{70, 80});

		assertEquals(expected, actual);
	}

	@Test
	void mapTest() {

		List<String> expected = Arrays.asList("10lol", "20lol", "20lol", "25lol");
		List<String> actual = new ArrayList<>();

		Subject<Integer> subject = Subject.single();

		Observable<Integer> observable = subject.asObservable();

		Subscription subscription = observable.map(integer -> integer + "lol").subscribe(actual::add);

		subject.next(10);
		observable.map(integer -> integer + "lol").one().subscribe(actual::add);

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

		Subject<Integer> subject = Subject.single();

		Observable<Integer> observable = subject.asObservable();

		Subject<Object> untilSubject = Subject.single();

		Observable<?> untilObservable = untilSubject.asObservable();

		observable.takeUntil(untilObservable).subscribe(actual::add);

		subject.next(10);
		subject.next(20);
		subject.next(25);

		untilSubject.next(new Object());

		subject.next(50);
		subject.next(60);

		assertEquals(expected, actual);
	}
}

