package com.github.tix320.kiwi.test.reactive;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.Flow.Subscription;
import java.util.concurrent.SubmissionPublisher;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
class BufferPublisherTest {

	@Test
	void simpleTest()
			throws InterruptedException {

		List<String> expected = Arrays.asList("a4", "a5", "a6", "a7", "a8", "b4", "b5", "b6", "b7", "b8", "a9", "b9",
				"c5", "c6", "c7", "a10", "b10", "a11", "b11", "a12", "b12", "a13", "b13", "d9", "a14", "b14", "d14",
				"a15", "b15");

		List<String> actual = new ArrayList<>();

		ExecutorService executor = Executors.newCachedThreadPool(BufferPublisherTest::daemonThread);
		SubmissionPublisher<String> submissionPublisher = new SubmissionPublisher<>(executor, 5);
		// Publisher<String> publisher = Publisher.buffered(5);
		// Observable<String> observable = publisher.asObservable();
		submissionPublisher.submit("4");
		submissionPublisher.submit("5");
		submissionPublisher.submit("6");

		submissionPublisher.subscribe(new Subscriber<String>() {
			@Override
			public void onSubscribe(Subscription subscription) {

			}

			@Override
			public void onNext(String item) {
				actual.add("a" + item);
			}

			@Override
			public void onError(Throwable throwable) {

			}

			@Override
			public void onComplete() {

			}
		});

		submissionPublisher.submit("7");
		submissionPublisher.submit("8");

		submissionPublisher.subscribe(new Subscriber<String>() {
			@Override
			public void onSubscribe(Subscription subscription) {

			}

			@Override
			public void onNext(String item) {
				actual.add("b" + item);
			}

			@Override
			public void onError(Throwable throwable) {

			}

			@Override
			public void onComplete() {

			}
		});

		submissionPublisher.submit("9");

		submissionPublisher.subscribe(new Subscriber<String>() {
			@Override
			public void onSubscribe(Subscription subscription) {
				subscription.request(3);
			}

			@Override
			public void onNext(String item) {
				actual.add("c" + item);
			}

			@Override
			public void onError(Throwable throwable) {

			}

			@Override
			public void onComplete() {

			}
		});

		submissionPublisher.submit("10");
		submissionPublisher.submit("11");
		submissionPublisher.submit("12");
		submissionPublisher.submit("13");
		submissionPublisher.subscribe(new Subscriber<String>() {

			Subscription subscription;

			@Override
			public void onSubscribe(Subscription subscription) {
				subscription.request(1);
				this.subscription = subscription;
			}

			@Override
			public void onNext(String item) {
				actual.add("d" + item);
				subscription.cancel();
			}

			@Override
			public void onError(Throwable throwable) {

			}

			@Override
			public void onComplete() {

			}
		});
		// executor.shutdown();

		submissionPublisher.submit("14");
		submissionPublisher.submit("15");

		submissionPublisher.close();
		assertEquals(expected, actual);
	}

	@Test
	void concurrentTest() {
		Publisher<String> publisher = Publisher.buffered(15);
		Observable<String> observable = publisher.asObservable();

		Stream.iterate('a', character -> (char) (character + 1))
				.limit(500)
				.parallel()
				.unordered()
				.forEach(character -> {
					observable.subscribe(s -> s = "test");
					publisher.publish("1");
				});
	}

	private static Thread daemonThread(Runnable runnable) {
		Thread thread = new Thread(runnable);
		thread.setDaemon(true);
		return thread;
	}
}
