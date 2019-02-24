package io.titix.kiwi.rx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author tix32 on 24-Feb-19
 */
public class ObservableTest {

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
}
