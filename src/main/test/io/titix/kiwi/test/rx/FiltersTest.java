package io.titix.kiwi.test.rx;

import java.util.concurrent.atomic.AtomicReference;

import io.titix.kiwi.rx.Observable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
class FiltersTest {


	@Test
	void filterOnCompleteTest() {
		AtomicReference<String> actual = new AtomicReference<>("");

		Observable.of(10).map(integer -> integer + 5)
				.join(integer -> integer + "", ",", "(", ")")
				.subscribe(actual::set);

		assertEquals("(15)", actual.get());
	}

	@Test
	void filterIllegalObservableTest() {
		Observable<Integer> observable3 = consumer -> null;
		assertThrows(IllegalArgumentException.class, observable3::one);
	}
}
