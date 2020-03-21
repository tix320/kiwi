package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.util.None;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 21-Mar-20.
 */
public class UntilObservableTest {

	@Test
	void takeUntilAlreadyCompletedObservableTest() {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();
		untilPublisher.complete();

		List<String> called = new ArrayList<>();

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(new Subscriber<Integer>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				called.add("onSubscribe");
			}

			@Override
			public boolean onPublish(Integer item) {
				return false;
			}

			@Override
			public boolean onError(Throwable throwable) {
				return false;
			}

			@Override
			public void onComplete() {
				called.add("onComplete");
			}
		});


		assertEquals(2, called.size());
		assertEquals("onSubscribe", called.get(0));
		assertEquals("onComplete", called.get(1));
	}

	@Test
	void publishOnSubscribeTest() {
		Publisher<Integer> publisher = Publisher.simple();

		Publisher<None> untilPublisher = Publisher.simple();
		untilPublisher.complete();

		List<String> called = new ArrayList<>();

		publisher.asObservable().takeUntil(untilPublisher.asObservable()).subscribe(new Subscriber<Integer>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				called.add("onSubscribe");
				publisher.publish(10);
			}

			@Override
			public boolean onPublish(Integer item) {
				called.add("onPublish");
				return false;
			}

			@Override
			public boolean onError(Throwable throwable) {
				called.add("onError");
				return false;
			}

			@Override
			public void onComplete() {
				called.add("onComplete");
			}
		});


		assertEquals(2, called.size());
		assertEquals("onSubscribe", called.get(0));
		assertEquals("onComplete", called.get(1));
	}
}
