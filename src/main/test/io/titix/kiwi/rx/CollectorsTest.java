package io.titix.kiwi.rx;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran.Sargsyan on 28-Feb-19
 */
class CollectorsTest {

	@Test
	void toMapTest() {

		Observable.of("a", "aa", "aaa", "aaaa").toMap(String::length, s -> s).subscribe(map -> {
			assertEquals(Map.of(1, "a", 2, "aa", 3, "aaa", 4, "aaaa"), map);
		});


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
}
