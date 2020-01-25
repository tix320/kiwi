package com.github.tix320.kiwi.test.observable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.subject.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran.Sargsyan on 28-Feb-19
 */
class CollectorsTest {

	@Test
	void toMapTest() {
		AtomicReference<Map<Integer, String>> actual = new AtomicReference<>();
		Observable.of("a", "aa", "aaa", "aaaa").toMap(String::length, s -> s).subscribe(actual::set);

		assertEquals(Map.of(1, "a", 2, "aa", 3, "aaa", 4, "aaaa"), actual.get());
	}

	@Test
	void toMapTest2() {
		Map<Integer, String> actualMap = new HashMap<>();

		Publisher<Integer> publisher = Publisher.buffered(2);

		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(1);
		publisher.publish(2);
		publisher.publish(3);

		observable.toMap(integer -> integer, integer -> integer + "").subscribe(map -> map.forEach(actualMap::put));

		assertEquals(Map.of(), actualMap);

		publisher.publish(4);
		publisher.publish(5);

		assertEquals(Map.of(), actualMap);

		publisher.publish(6);
		publisher.complete();

		assertEquals(Map.of(2, "2", 3, "3", 4, "4", 5, "5", 6, "6"), actualMap);
	}

	@Test
	void toListTest() {
		AtomicReference<List<Integer>> actual = new AtomicReference<>();
		Observable.of(1, 2, 3, 4).toList().subscribe(actual::set);

		assertEquals(List.of(1, 2, 3, 4), actual.get());
	}

	@Test
	void joinTest() {
		AtomicReference<String> actual = new AtomicReference<>("");

		Publisher<Integer> publisher = Publisher.buffered(2);

		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(1);
		publisher.publish(2);
		publisher.publish(3);

		observable.join(integer -> integer + "", ",").subscribe(actual::set);
		assertEquals("", actual.get());

		publisher.publish(4);
		publisher.publish(5);

		assertEquals("", actual.get());

		publisher.publish(6);
		publisher.complete();

		assertEquals("2,3,4,5,6", actual.get());
	}

	@Test
	void joinWithParamsTest() {
		AtomicReference<String> actual = new AtomicReference<>("");

		Publisher<Integer> publisher = Publisher.buffered(2);

		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(1);
		publisher.publish(2);
		publisher.publish(3);

		observable.join(integer -> integer + "", ",", "[", "]").subscribe(actual::set);
		assertEquals("", actual.get());

		publisher.publish(4);
		publisher.publish(5);

		assertEquals("", actual.get());

		publisher.publish(6);
		publisher.complete();

		assertEquals("[2,3,4,5,6]", actual.get());
	}

	@Test
	void doubleCollectorTest() {
		AtomicReference<Map<Integer, String>> actual = new AtomicReference<>(Map.of());

		Observable.of("hello").join(s -> s, ",", "[", "]").toMap(String::length, s -> s).subscribe(actual::set);

		assertEquals(Map.of(7, "[hello]"), actual.get());
	}

	@Test
	void decorateCollectorTest() {
		AtomicReference<Integer> actual = new AtomicReference<>();

		Observable.of(1, 2, 3)
				.toMap(Function.identity(), value -> value * 2)
				.map(map -> map.get(3))
				.subscribe(actual::set);

		assertEquals(6, actual.get());
	}
}
