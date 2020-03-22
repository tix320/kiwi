package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
class SimplePublisherTest {

	@Test
	void oneObservableTest() {
		List<Integer> expected = Arrays.asList(6, 7);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();

		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(3);
		publisher.publish(4);

		Subscription subscription = observable.subscribe(actual::add);

		publisher.publish(6);
		publisher.publish(7);

		subscription.unsubscribe();

		publisher.publish(8);

		assertEquals(expected, actual);
	}

	@Test
	void anyTypeObservables() {

		List<Integer> expected = Arrays.asList(7, 8, 9, 9, 10, 10, 10, 11, 11, 11, 12, 12, 12, 13, 13, 14, 14, 14, 15,
				15);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(4);
		publisher.publish(5);
		publisher.publish(6);

		observable.subscribe(actual::add);

		publisher.publish(7);
		publisher.publish(8);

		observable.subscribe(actual::add);

		publisher.publish(9);

		observable.take(3).subscribe(actual::add);

		publisher.publish(10);
		publisher.publish(11);
		publisher.publish(12);
		publisher.publish(13);

		observable.toMono().subscribe(actual::add);

		publisher.publish(14);
		publisher.publish(15);

		assertEquals(expected, actual);
	}

	@Test
	void callAsObservableMore() {

		List<Integer> expected = Arrays.asList(3, 3, 3, 3, 4, 4, 4, 4, 5, 5, 5, 5);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.simple();
		Observable<Integer> observable1 = publisher.asObservable();
		Observable<Integer> observable2 = publisher.asObservable();

		observable1.subscribe(actual::add);
		observable1.subscribe(actual::add);
		observable2.subscribe(actual::add);
		observable2.subscribe(actual::add);

		publisher.publish(3);
		publisher.publish(4);
		publisher.publish(5);

		assertEquals(expected, actual);
	}

	@Test
	void concurrentTest() {
		Publisher<Character> publisher = Publisher.simple();
		Observable<Character> observable = publisher.asObservable();

		List<Character> wtf = new ArrayList<>();
		Stream.iterate('a', character -> (char) (character + 1))
				.limit(500)
				.parallel()
				.unordered()
				.forEach(character -> {
					observable.subscribe(wtf::add);
					publisher.publish(character);
				});
	}
}
