package io.titix.kiwi.test.rx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import io.titix.kiwi.rx.observable.Observable;
import io.titix.kiwi.rx.subject.Subject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran.Sargsyan on 28-Feb-19
 */
class CollectorsTest {

	@Test
	void toMapTest() {
		AtomicReference<Map<Integer, String>> actual = new AtomicReference<>();
		Observable.of("a", "aa", "aaa", "aaaa")
				.toMap(String::length, s -> s)
				.subscribe(actual::set);

		assertEquals(Map.of(1, "a", 2, "aa", 3, "aaa", 4, "aaaa"), actual.get());
	}

	@Test
	void toMapTest2() {
		Map<Integer, String> actualMap = new HashMap<>();

		Subject<Integer> subject = Subject.buffered(2);

		Observable<Integer> observable = subject.asObservable();

		subject.next(1);
		subject.next(2);
		subject.next(3);

		observable.toMap(integer -> integer, integer -> integer + "").subscribe(map -> map.forEach(actualMap::put));

		assertEquals(Map.of(), actualMap);

		subject.next(4);
		subject.next(5);

		assertEquals(Map.of(), actualMap);

		subject.next(6);
		subject.complete();

		assertEquals(Map.of(2, "2", 3, "3", 4, "4", 5, "5", 6, "6"), actualMap);
	}

	@Test
	void toListTest() {
		AtomicReference<List<Integer>> actual = new AtomicReference<>();
		Observable.of(1, 2, 3, 4)
				.toList()
				.subscribe(actual::set);

		assertEquals(List.of(1, 2, 3, 4), actual.get());
	}

	@Test
	void joinTest() {
		AtomicReference<String> actual = new AtomicReference<>("");

		Subject<Integer> subject = Subject.buffered(2);

		Observable<Integer> observable = subject.asObservable();

		subject.next(1);
		subject.next(2);
		subject.next(3);

		observable.join(integer -> integer + "", ",").subscribe(actual::set);
		assertEquals("", actual.get());

		subject.next(4);
		subject.next(5);

		assertEquals("", actual.get());

		subject.next(6);
		subject.complete();

		assertEquals("2,3,4,5,6", actual.get());
	}

	@Test
	void joinWithParamsTest() {
		AtomicReference<String> actual = new AtomicReference<>("");

		Subject<Integer> subject = Subject.buffered(2);

		Observable<Integer> observable = subject.asObservable();

		subject.next(1);
		subject.next(2);
		subject.next(3);

		observable.join(integer -> integer + "", ",", "[", "]").subscribe(actual::set);
		assertEquals("", actual.get());

		subject.next(4);
		subject.next(5);

		assertEquals("", actual.get());

		subject.next(6);
		subject.complete();

		assertEquals("[2,3,4,5,6]", actual.get());
	}

	@Test
	void doubleCollectorTest() {
		AtomicReference<Map<Integer, String>> actual = new AtomicReference<>(Map.of());

		Observable.of("hello").join(s -> s, ",", "[", "]").toMap(String::length, s -> s).subscribe(actual::set);

		assertEquals(Map.of(7, "[hello]"), actual.get());
	}
}
