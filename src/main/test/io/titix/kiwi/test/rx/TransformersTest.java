package io.titix.kiwi.test.rx;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import io.titix.kiwi.rx.observable.Observable;
import io.titix.kiwi.rx.observable.transform.Result;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
	void decoratorIllegalObservableTest() {
		Observable<Integer> observable3 = consumer -> null;
		assertThrows(UnsupportedOperationException.class, observable3::one);
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
}
