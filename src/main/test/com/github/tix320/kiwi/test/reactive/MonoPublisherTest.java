package com.github.tix320.kiwi.test.reactive;

import java.util.concurrent.atomic.AtomicBoolean;

import com.github.tix320.kiwi.api.reactive.observable.MonoObservable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.MonoPublisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MonoPublisherTest {

	@Test
	void simpleTest() {
		MonoPublisher<String> mono = Publisher.mono();
		MonoObservable<String> observable = mono.asObservable();
		AtomicBoolean called = new AtomicBoolean(false);
		observable.subscribe(s -> {
			assertEquals("foo", s);
			called.set(true);
		});
		mono.publish("foo");
		assertTrue(called.get());
	}

	@Test
	void subscribeAfterPublish() {
		MonoPublisher<String> mono = Publisher.mono();
		MonoObservable<String> observable = mono.asObservable();
		AtomicBoolean called = new AtomicBoolean(false);
		mono.publish("foo");
		observable.subscribe(s -> {
			assertEquals("foo", s);
			called.set(true);
		});
		assertTrue(called.get());
	}

	@Test
	void emptyContentTest() {
		MonoPublisher<String> mono = new MonoPublisher<>();

		assertTrue(mono.getContent().isEmpty());
	}

	@Test
	void valueContentTest() {
		MonoPublisher<String> mono = new MonoPublisher<>();
		mono.publish("foo");

		assertTrue(mono.getContent().isPresent());
		String content = mono.getContent().get();
		assertEquals("foo", content);
	}
}
