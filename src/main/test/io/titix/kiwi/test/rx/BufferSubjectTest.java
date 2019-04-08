package io.titix.kiwi.test.rx;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import io.titix.kiwi.rx.observable.Observable;
import io.titix.kiwi.rx.subject.Subject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author tix32 on 23-Feb-19
 */
class BufferSubjectTest {

	@Test
	void simpleTest() {

		List<String> expected = Arrays.asList("a4", "a5", "a6", "a7", "a8", "b4", "b5", "b6", "b7", "b8", "a9", "b9", "c5", "c6", "c7", "a10", "b10", "a11", "b11", "a12", "b12", "a13", "b13", "d9", "a14", "b14", "a15", "b15");

		List<String> actual = new ArrayList<>();

		Subject<String> subject = Subject.buffered(5);
		Observable<String> observable = subject.asObservable();

		subject.next("4");
		subject.next("5");
		subject.next("6");

		observable.subscribe(s -> actual.add("a" + s));

		subject.next("7");
		subject.next("8");

		observable.subscribe(s -> actual.add("b" + s));

		subject.next("9");

		observable.take(3).subscribe(s -> actual.add("c" + s));

		subject.next("10");
		subject.next("11");
		subject.next("12");
		subject.next("13");

		observable.one().subscribe(s -> actual.add("d" + s));

		subject.next("14");
		subject.next("15");

		assertEquals(expected, actual);
	}

	@Test
	void concurrentTest() {
		Subject<String> subject = Subject.buffered(15);
		Observable<String> observable = subject.asObservable();

		Stream.iterate('a', character -> (char) (character + 1))
				.limit(500)
				.parallel()
				.unordered()
				.forEach(character -> {
					observable.subscribe(s -> s = "test");
					subject.next("1");
				});
	}
}
