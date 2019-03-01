package io.titix.kiwi.test.rx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subject;
import io.titix.kiwi.rx.Subscription;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author tix32 on 23-Feb-19
 */
class SingleSubjectTest {

	@Test
	void oneObservableTest() {
		List<Integer> expected = Arrays.asList(6, 7);
		List<Integer> actual = new ArrayList<>();

		Subject<Integer> subject = Subject.single();

		Observable<Integer> observable = subject.asObservable();

		subject.next(3);
		subject.next(4);

		Subscription subscription = observable.subscribe(actual::add);

		subject.next(6);
		subject.next(7);

		subscription.unsubscribe();

		subject.next(8);

		assertEquals(expected, actual);
	}

	@Test
	void anyTypeObservables() {

		List<Integer> expected = Arrays.asList(7, 8, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12, 13, 13, 14, 14, 14, 15, 15);
		List<Integer> actual = new ArrayList<>();

		Subject<Integer> subject = Subject.single();
		Observable<Integer> observable = subject.asObservable();

		subject.next(4);
		subject.next(5);
		subject.next(6);

		observable.subscribe(actual::add);

		subject.next(7);
		subject.next(8);

		observable.subscribe(actual::add);

		subject.next(9);

		observable.take(3).subscribe(actual::add);

		subject.next(10);
		subject.next(11);
		subject.next(12);
		subject.next(13);

		observable.one().subscribe(actual::add);

		subject.next(14);
		subject.next(15);

		assertEquals(expected, actual);
	}

	@Test
	void callAsObservableMore() {

		List<Integer> expected = Arrays.asList(3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5);
		List<Integer> actual = new ArrayList<>();

		Subject<Integer> subject = Subject.single();
		Observable<Integer> observable1 = subject.asObservable();
		Observable<Integer> observable2 = subject.asObservable();

		observable1.subscribe(actual::add);
		observable1.subscribe(actual::add);
		observable2.subscribe(actual::add);
		observable2.subscribe(actual::add);

		subject.next(3);
		subject.next(4);
		subject.next(5);

		assertEquals(expected, actual);
	}

	@Test
	void concurrentTest() {
		Subject<String> subject = Subject.single();
		Observable<String> observable = subject.asObservable();

		Stream.iterate('a', character -> (char) (character + 1))
				.limit(500)
				.parallel()
				.unordered()
				.forEach(character -> {
					observable.subscribe(s -> s = "test");
					subject.next("1");
				});
	}
}
