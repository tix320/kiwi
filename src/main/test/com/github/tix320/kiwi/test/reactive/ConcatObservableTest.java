package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ConcatObservableTest {

	@Test
	public void concatWithCompletingOneOf() {
		List<Integer> expected = Arrays.asList(3, 5, 9, 8);
		List<Integer> actual = new ArrayList<>();

		Observable<Integer> observable1 = Observable.of(3);
		Observable<Integer> observable2 = Observable.of(5, 6);
		Publisher<Integer> publisher = Publisher.cached();
		publisher.publish(9);

		Observable<Integer> observable3 = publisher.asObservable();

		Observable.concat(observable1, observable2, observable3).conditionalSubscribe(number -> {
			actual.add(number);
			return number != 5;
		});

		publisher.publish(8);

		assertEquals(expected, actual);
	}
}
