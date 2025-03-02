package com.github.tix320.kiwi.reactive;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.tix320.kiwi.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 08-Apr-20.
 */
public class GetOnTimoutObservableTest {

	@Test
	public void publishBeforeTimoutTest() throws InterruptedException {
		List<Integer> expected = Collections.singletonList(1);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher = Publisher.simple();

		publisher.asObservable().getOnTimout(Duration.ofSeconds(2), () -> 5).subscribe(actual::add);

		Thread.sleep(100);

		publisher.publish(1);
		publisher.publish(2);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}

	@Test
	public void publishAfterTimoutTest() throws InterruptedException {
		List<Integer> expected = Collections.singletonList(5);
		List<Integer> actual = Collections.synchronizedList(new ArrayList<>());

		Publisher<Integer> publisher = Publisher.simple();

		publisher.asObservable().getOnTimout(Duration.ofMillis(500), () -> 5).subscribe(actual::add);

		Thread.sleep(1000);

		publisher.publish(1);
		publisher.publish(2);

		Thread.sleep(100);

		assertEquals(expected, actual);
	}
}
