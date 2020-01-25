package com.github.tix320.kiwi.test.observable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.github.tix320.kiwi.api.observable.Observable;
import com.github.tix320.kiwi.api.observable.subject.Publisher;
import com.github.tix320.kiwi.internal.observable.CompletedException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
class PublisherTest {

	@Test
	void iterableTest() {
		List<String> expected = Arrays.asList("2", "1", "0");
		List<String> actual = new ArrayList<>();


		Publisher<String> publisher = Publisher.simple();
		Observable<String> observable = publisher.asObservable();
		observable.subscribe(actual::add);

		publisher.publish(() -> new Iterator<>() {
			int index = 3;

			@Override
			public boolean hasNext() {
				return index-- > 0;
			}

			@Override
			public String next() {
				return index + "";
			}
		});

		assertEquals(expected, actual);
	}

	@Test
	void completeTest() {
		Publisher<Integer> publisher = Publisher.simple();

		publisher.publish(1);
		publisher.publish(2);
		publisher.publish(3);
		publisher.complete();
		assertThrows(CompletedException.class, () -> publisher.publish(4));
	}
}
