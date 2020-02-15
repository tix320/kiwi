package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ZipObservableTest {

	@Test
	void zipOnCompleteTest() {
		List<List<Integer>> expected = Arrays.asList(Arrays.asList(6, 4), Arrays.asList(9, 7),
				Collections.singletonList(25));
		List<List<Integer>> actual = new ArrayList<>();

		Publisher<Integer> publisher1 = Publisher.simple();
		Publisher<Integer> publisher2 = Publisher.single(4);

		Observable.zip(publisher1.asObservable(), publisher2.asObservable())
				.subscribe(actual::add, () -> actual.add(Collections.singletonList(25)));

		publisher1.publish(6);
		publisher2.publish(7);

		publisher1.publish(9);

		publisher2.complete();

		publisher1.publish(10);

		assertEquals(expected, actual);
	}
}
