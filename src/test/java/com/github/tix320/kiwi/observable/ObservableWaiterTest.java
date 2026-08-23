package com.github.tix320.kiwi.observable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tix320.skimp.exception.ThreadInterruptedException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class ObservableWaiterTest {

	@Test
	public void awaitReturnsOnCompletionWithoutCancelling() throws Exception {
		ControlledObservable<Object> observable = new ControlledObservable<>();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread waitingThread = Thread.ofPlatform().start(() -> {
			try {
				observable.await(Duration.ofSeconds(5));
			} catch (Throwable throwable) {
				failure.set(throwable);
			}
		});

		assertTrue(observable.subscribed.await(5, TimeUnit.SECONDS));
		observable.complete();
		waitingThread.join(5000);

		assertFalse(waitingThread.isAlive());
		assertNull(failure.get());
		assertEquals(1, observable.unboundedRequests.get());
		assertEquals(0, observable.cancellations.get());
	}

	@Test
	public void awaitTimeoutCancelsSubscription() {
		ControlledObservable<Object> observable = new ControlledObservable<>();

		assertThrows(TimeoutException.class, () -> observable.await(Duration.ofMillis(1)));

		assertEquals(1, observable.unboundedRequests.get());
		assertEquals(1, observable.cancellations.get());
	}

	@Test
	public void waitAndApplyIgnoresItemPublishedAfterTimeout() {
		ControlledObservable<String> observable = new ControlledObservable<>();
		AtomicInteger consumed = new AtomicInteger();

		assertThrows(TimeoutException.class,
			() -> observable.waitAndApply(Duration.ofMillis(1), item -> consumed.incrementAndGet()));
		observable.publish("late");

		assertEquals(0, consumed.get());
		assertEquals(1, observable.cancellations.get());
	}

	@Test
	public void interruptionCancelsSubscriptionAndRestoresInterruptFlag() throws Exception {
		ControlledObservable<Object> observable = new ControlledObservable<>();
		AtomicReference<Throwable> failure = new AtomicReference<>();
		AtomicBoolean interrupted = new AtomicBoolean(false);
		Thread waitingThread = Thread.ofPlatform().start(() -> {
			try {
				observable.await(Duration.ofSeconds(30));
			} catch (Throwable throwable) {
				failure.set(throwable);
				interrupted.set(Thread.currentThread().isInterrupted());
			}
		});

		assertTrue(observable.subscribed.await(5, TimeUnit.SECONDS));
		waitingThread.interrupt();
		waitingThread.join(5000);

		assertFalse(waitingThread.isAlive());
		assertInstanceOf(ThreadInterruptedException.class, failure.get());
		assertTrue(interrupted.get());
		assertEquals(1, observable.cancellations.get());
	}

	@Test
	public void getReturnsNullWhenSourceCompletesWithoutItem() throws Exception {
		ControlledObservable<Object> observable = new ControlledObservable<>();
		AtomicReference<Object> result = new AtomicReference<>(new Object());
		AtomicReference<Throwable> failure = new AtomicReference<>();
		Thread waitingThread = Thread.ofPlatform().start(() -> {
			try {
				result.set(observable.get(Duration.ofSeconds(5)));
			} catch (Throwable throwable) {
				failure.set(throwable);
			}
		});

		assertTrue(observable.subscribed.await(5, TimeUnit.SECONDS));
		observable.complete();
		waitingThread.join(5000);

		assertFalse(waitingThread.isAlive());
		assertNull(failure.get());
		assertNull(result.get());
	}

	private static final class ControlledObservable<T> extends Observable<T> {

		private final CountDownLatch subscribed = new CountDownLatch(1);
		private final AtomicInteger unboundedRequests = new AtomicInteger();
		private final AtomicInteger cancellations = new AtomicInteger();
		private final AtomicReference<Subscriber<? super T>> subscriber = new AtomicReference<>();

		@Override
		public void subscribe(Subscriber<? super T> value) {
			subscriber.set(value);
			value.setSubscription(new Subscription() {
				@Override
				protected void onRequest(long count) {
				}

				@Override
				protected void onUnboundRequest() {
					unboundedRequests.incrementAndGet();
				}

				@Override
				protected void onCancel(Unsubscription unsubscription) {
					cancellations.incrementAndGet();
				}
			});
			subscribed.countDown();
		}

		void publish(T item) {
			subscriber.get().publish(item);
		}

		void complete() {
			subscriber.get().complete(SourceCompletion.DEFAULT);
		}

	}

}
