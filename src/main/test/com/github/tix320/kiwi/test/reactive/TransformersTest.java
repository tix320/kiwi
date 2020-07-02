package com.github.tix320.kiwi.test.reactive;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
class TransformersTest {

	@Test
	void decoratorOnCompleteTest() throws InterruptedException {
		AtomicReference<String> actual = new AtomicReference<>("");

		Observable.of(10)
				.map(integer -> integer + 5)
				.join(integer -> integer + "", ",", "(", ")")
				.subscribe(actual::set);

		Thread.sleep(100);

		assertEquals("(15)", actual.get());
	}

	@Test
	void filterTest() throws InterruptedException {
		AtomicReference<List<Integer>> actual = new AtomicReference<>();
		Observable.of(1, 2, 3, 4, 5).filter(integer -> integer > 2).toList().subscribe(actual::set);

		Thread.sleep(100);

		assertEquals(List.of(3, 4, 5), actual.get());
	}
}
