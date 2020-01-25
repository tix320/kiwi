package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
class BufferPublisherTest {

	@Test
	void simpleTest() {

		List<String> expected = Arrays.asList("a4", "a5", "a6", "a7", "a8", "b4", "b5", "b6", "b7", "b8", "a9", "b9",
				"c5", "c6", "c7", "a10", "b10", "a11", "b11", "a12", "b12", "a13", "b13", "d9", "a14", "b14", "a15",
				"b15");

		List<String> actual = new ArrayList<>();

		Publisher<String> publisher = Publisher.buffered(5);
		Observable<String> observable = publisher.asObservable();

		publisher.publish("4");
		publisher.publish("5");
		publisher.publish("6");

		observable.subscribe(s -> actual.add("a" + s));

		publisher.publish("7");
		publisher.publish("8");

		observable.subscribe(s -> actual.add("b" + s));

		publisher.publish("9");

		observable.take(3).subscribe(s -> actual.add("c" + s));

		publisher.publish("10");
		publisher.publish("11");
		publisher.publish("12");
		publisher.publish("13");

		observable.toMono().subscribe(s -> actual.add("d" + s));

		publisher.publish("14");
		publisher.publish("15");

		assertEquals(expected, actual);
	}

	@Test
	void concurrentTest() {
		Publisher<String> publisher = Publisher.buffered(15);
		Observable<String> observable = publisher.asObservable();

		Stream.iterate('a', character -> (char) (character + 1))
				.limit(500)
				.parallel()
				.unordered()
				.forEach(character -> {
					observable.subscribe(s -> s = "test");
					publisher.publish("1");
				});
	}
}
