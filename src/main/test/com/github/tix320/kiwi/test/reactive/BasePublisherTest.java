package com.github.tix320.kiwi.test.reactive;

import java.lang.reflect.Field;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.BufferedPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 14-Jul-20.
 */
public class BasePublisherTest {

	@Test
	public void bufferedCleanupTest() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
		BufferedPublisher<Integer> publisher = Publisher.buffered(5);

		Observable<Integer> observable = publisher.asObservable();

		for (int i = 0; i < 10; i++) {
			observable.subscribe(integer -> {
				try {
					Thread.sleep(200);
				}
				catch (InterruptedException e) {
					throw new IllegalStateException(e);
				}
			});
		}

		Field queueField = BasePublisher.class.getDeclaredField("queue");
		queueField.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<Integer> queue = (List<Integer>) queueField.get(publisher);

		publisher.publish(1);
		publisher.publish(2);
		publisher.publish(3);
		publisher.publish(4);
		publisher.publish(5);
		publisher.publish(6);
		publisher.publish(7);
		publisher.publish(8);
		publisher.publish(9);

		Thread.sleep(2000);

		publisher.publish(10);

		Thread.sleep(2000);

		List<Integer> expected = List.of(5, 6, 7, 8, 9, 10);

		assertEquals(expected, queue);
	}

	@Test
	public void bufferedDoubleCleanupTest() throws InterruptedException, NoSuchFieldException, IllegalAccessException {
		BufferedPublisher<Integer> publisher = Publisher.buffered(3);

		Observable<Integer> observable = publisher.asObservable();

		for (int i = 0; i < 10; i++) {
			observable.subscribe(integer -> {
				try {
					Thread.sleep(200);
				}
				catch (InterruptedException e) {
					throw new IllegalStateException(e);
				}
			});
		}

		Field queueField = BasePublisher.class.getDeclaredField("queue");
		queueField.setAccessible(true);
		@SuppressWarnings("unchecked")
		List<Integer> queue = (List<Integer>) queueField.get(publisher);

		publisher.publish(1);
		publisher.publish(2);
		publisher.publish(3);
		publisher.publish(4);
		publisher.publish(5);

		Thread.sleep(2000);

		publisher.publish(6);

		Thread.sleep(2000);

		List<Integer> expected = List.of(3, 4, 5, 6);

		assertEquals(expected, queue);

		publisher.publish(7);
		publisher.publish(8);
		publisher.publish(9);
		publisher.publish(10);
		publisher.publish(11);

		Thread.sleep(2000);

		publisher.publish(12);

		Thread.sleep(2000);

		expected = List.of(9, 10, 11, 12);

		assertEquals(expected, queue);
	}
}
