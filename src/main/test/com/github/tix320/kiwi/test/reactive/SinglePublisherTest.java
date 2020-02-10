package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SinglePublisherTest {

	@Test
	public void singleTest() {
		List<Integer> expected = Arrays.asList(10, 15, 19, 20, 24);
		List<Integer> actual = new ArrayList<>();

		Publisher<Integer> publisher = Publisher.single(10);
		Observable<Integer> observable = publisher.asObservable();
		observable.subscribe(actual::add);
		publisher.publish(15);
		observable.subscribe(number -> actual.add(number + 4));
		publisher.publish(20);

		assertEquals(expected, actual);
	}
}
