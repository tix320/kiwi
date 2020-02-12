package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CachedPublisherTest {

	@Test
	public void simpleTest() {
		List<Integer> expected = Arrays.asList(10, 20, 30, 60, 40, 120);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.cached();

		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(10);

		observable.subscribe(actual::add);

		publisher.publish(20);

		observable.map(integer -> integer * 3).subscribe(actual::add);

		publisher.publish(40);

		assertEquals(expected, actual);
	}
}
