package com.gitlab.tixtix320.kiwi.test.rx;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.decorator.single.transform.Result;
import com.gitlab.tixtix320.kiwi.observable.subject.Subject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
class TransformersTest {

	@Test
	void decoratorOnCompleteTest() {
		AtomicReference<String> actual = new AtomicReference<>("");

		Observable.of(10).map(integer -> integer + 5)
				.join(integer -> integer + "", ",", "(", ")")
				.subscribe(actual::set);

		assertEquals("(15)", actual.get());
	}

	@Test
	void filterTest() {
		AtomicReference<List<Integer>> actual = new AtomicReference<>();
		Observable.of(1, 2, 3, 4, 5).filter(integer -> integer > 2).toList().subscribe(actual::set);
		assertEquals(List.of(3, 4, 5), actual.get());
	}

	@Test
	void customTransformerTest() {
		AtomicReference<List<String>> actual = new AtomicReference<>();
		Observable.of(1, 2, 3, 4, 5, 6, 7).transform((subscription, integer) -> {
			if (integer > 5) {
				return Result.none();
			}
			else {
				return Result.of(String.valueOf(integer));
			}
		}).toList().subscribe(actual::set);
		assertEquals(List.of("1", "2", "3", "4", "5"), actual.get());
	}

	@Test
	void customTransformerWithBufferedSubject() {
		AtomicReference<List<Integer>> actual = new AtomicReference<>();
		Subject<Integer> subject = Subject.buffered(3);
		Observable<Integer> observable = subject.asObservable();
		subject.next(4);
		subject.next(2);
		subject.complete();
		observable.transform((subscription, integer) -> {
			subscription.unsubscribe();
			return Result.of(integer + 2);
		}).toList().subscribe(actual::set);
		assertEquals(List.of(6), actual.get());
	}
}
