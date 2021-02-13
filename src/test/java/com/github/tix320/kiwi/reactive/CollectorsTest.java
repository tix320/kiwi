package com.github.tix320.kiwi.reactive;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran.Sargsyan on 28-Feb-19
 */
public class CollectorsTest {

	@Test
	public void toMapTest() throws InterruptedException {
		AtomicReference<Map<Integer, String>> actual = new AtomicReference<>();
		Observable.of("a", "aa", "aaa", "aaaa").toMap(String::length, s -> s).subscribe(actual::set);

		Thread.sleep(100);

		assertEquals(Map.of(1, "a", 2, "aa", 3, "aaa", 4, "aaaa"), actual.get());
	}

	@Test
	public void toMapTest2() throws InterruptedException {
		Map<Integer, String> actualMap = new HashMap<>();

		Publisher<Integer> publisher = Publisher.buffered(2);

		Observable<Integer> observable = publisher.asObservable();

		publisher.publish(1);
		publisher.publish(2);
		publisher.publish(3);

		observable.toMap(integer -> integer, integer -> integer + "").subscribe(map -> map.forEach(actualMap::put));

		assertEquals(Map.<Integer, String>of(), actualMap);

		publisher.publish(4);
		publisher.publish(5);

		assertEquals(Map.<Integer, String>of(), actualMap);

		publisher.publish(6);
		publisher.complete();

		Thread.sleep(100);

		assertEquals(Map.of(2, "2", 3, "3", 4, "4", 5, "5", 6, "6"), actualMap);
	}

	@Test
	public void toListTest() throws InterruptedException {
		AtomicReference<List<Integer>> actual = new AtomicReference<>();
		Observable.of(1, 2, 3, 4).toList().subscribe(actual::set);

		Thread.sleep(100);

		assertEquals(List.of(1, 2, 3, 4), actual.get());
	}

	@Test
	public void joinTest() throws InterruptedException {
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

		Thread.sleep(100);

		assertEquals("2,3,4,5,6", actual.get());
	}

	@Test
	public void joinWithParamsTest() throws InterruptedException {
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

		Thread.sleep(100);

		assertEquals("[2,3,4,5,6]", actual.get());
	}

	@Test
	public void doubleCollectorTest() throws InterruptedException {
		AtomicReference<Map<Integer, String>> actual = new AtomicReference<>(Map.of());

		Observable.of("hello").join(s -> s, ",", "[", "]").toMap(String::length, s -> s).subscribe(actual::set);

		Thread.sleep(100);

		assertEquals(Map.of(7, "[hello]"), actual.get());
	}

	@Test
	public void decorateCollectorTest() throws InterruptedException {
		AtomicReference<Integer> actual = new AtomicReference<>();

		Observable.of(1, 2, 3)
				.toMap(Function.identity(), value -> value * 2)
				.map(map -> map.get(3))
				.subscribe(actual::set);

		Thread.sleep(100);

		assertEquals(6, actual.get());
	}
}
