package com.github.tix320.kiwi.test.reactive;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MonoPublisherTest {

	@Test
	void simpleTest() throws InterruptedException {
		MonoPublisher<String> mono = Publisher.mono();
		MonoObservable<String> observable = mono.asObservable();
		AtomicBoolean called = new AtomicBoolean(false);
		observable.subscribe(s -> {
			assertEquals("foo", s);
			called.set(true);
		});
		mono.publish("foo");

		Thread.sleep(100);

		assertTrue(called.get());
	}

	@Test
	void subscribeAfterPublish() throws InterruptedException {
		MonoPublisher<String> mono = Publisher.mono();
		MonoObservable<String> observable = mono.asObservable();
		AtomicBoolean called = new AtomicBoolean(false);
		mono.publish("foo");
		observable.subscribe(s -> {
			assertEquals("foo", s);
			called.set(true);
		});

		Thread.sleep(100);

		assertTrue(called.get());
	}
}
