package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.IntStream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
class BufferedPublisherTest {

	@Test
	void simpleTest() throws InterruptedException {
		List<String> expected1 = List.of("a4", "a5", "a6", "a7", "a8", "a9", "a10", "a11", "a12", "a13", "a14", "a15");
		List<String> expected2 = List.of("b4", "b5", "b6", "b7", "b8", "b9", "b10", "b11", "b12", "b13", "b14", "b15");
		List<String> expected3 = List.of("c5", "c6", "c7");
		List<String> expected4 = List.of("d9");

		List<String> actual1 = new CopyOnWriteArrayList<>();
		List<String> actual2 = new CopyOnWriteArrayList<>();
		List<String> actual3 = new CopyOnWriteArrayList<>();
		List<String> actual4 = new CopyOnWriteArrayList<>();

		Publisher<String> publisher = Publisher.buffered(5);
		Observable<String> observable = publisher.asObservable();

		observable.subscribe(s -> actual1.add("a" + s));
		publisher.publish("4");
		publisher.publish("5");
		publisher.publish("6");


		publisher.publish("7");
		publisher.publish("8");

		observable.subscribe(s -> actual2.add("b" + s));

		publisher.publish("9");

		observable.take(3).subscribe(s -> actual3.add("c" + s));

		publisher.publish("10");
		publisher.publish("11");
		publisher.publish("12");
		publisher.publish("13");

		observable.toMono().subscribe(s -> actual4.add("d" + s));

		publisher.publish("14");
		publisher.publish("15");

		Thread.sleep(100);

		assertEquals(expected1, actual1);
		assertEquals(expected2, actual2);
		assertEquals(expected3, actual3);
		assertEquals(expected4, actual4);
	}

	@Test
	void concurrentTest() throws InterruptedException {
		Publisher<String> publisher = Publisher.buffered(15);
		Observable<String> observable = publisher.asObservable();

		int limit = 500;

		List<CopyOnWriteArrayList<String>> results = new ArrayList<>(limit);

		for (int i = 0; i < limit; i++) {
			results.add(new CopyOnWriteArrayList<>());
		}

		IntStream.range(0, 500).parallel().unordered().forEach(index -> {
			observable.subscribe(s -> results.get(index).add(index + ": " + s));
			publisher.publish("value" + index);
		});

		Thread.sleep(1000);
	}
}
