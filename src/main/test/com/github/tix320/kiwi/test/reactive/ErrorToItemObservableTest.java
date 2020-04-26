package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 26-Apr-20.
 */
public class ErrorToItemObservableTest {

	@Test
	void simpleTest() {
		List<String> expected = List.of("a", "error1", "b", "error2");
		List<String> actual = new ArrayList<>();

		Publisher<String> publisher = Publisher.simple();

		publisher.asObservable().mapErrorToItem(Throwable::getMessage).subscribe(actual::add);

		publisher.publish("a");
		publisher.publishError(new IllegalStateException("error1"));
		publisher.publish("b");
		publisher.publishError(new IllegalStateException("error2"));

		assertEquals(expected, actual);
	}
}
