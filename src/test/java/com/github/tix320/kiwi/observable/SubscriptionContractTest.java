package com.github.tix320.kiwi.observable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

public class SubscriptionContractTest {

	@Test
	public void repeatedUnboundedRequestsInvokeHookOnce() {
		TrackingSubscription subscription = new TrackingSubscription();

		subscription.requestUnbounded();
		subscription.requestUnbounded();
		subscription.requestUnbounded();

		assertEquals(1, subscription.unboundedRequests.get());
	}

	@Test
	public void concurrentUnboundedRequestsInvokeHookOnce() throws Exception {
		int threadCount = 16;
		TrackingSubscription subscription = new TrackingSubscription();
		CountDownLatch ready = new CountDownLatch(threadCount);
		CountDownLatch start = new CountDownLatch(1);
		CountDownLatch done = new CountDownLatch(threadCount);
		AtomicReference<Throwable> failure = new AtomicReference<>();

		for (int i = 0; i < threadCount; i++) {
			Thread.ofPlatform().start(() -> {
				ready.countDown();
				try {
					start.await();
					subscription.requestUnbounded();
				} catch (Throwable throwable) {
					failure.compareAndSet(null, throwable);
				} finally {
					done.countDown();
				}
			});
		}

		assertTrue(ready.await(5, TimeUnit.SECONDS));
		start.countDown();
		assertTrue(done.await(5, TimeUnit.SECONDS));
		assertNull(failure.get());
		assertEquals(1, subscription.unboundedRequests.get());
	}

	@Test
	public void unboundedRequestAfterCancellationIsRejected() {
		TrackingSubscription subscription = new TrackingSubscription();
		subscription.requestUnbounded();
		subscription.cancel();

		assertThrows(IllegalStateException.class, subscription::requestUnbounded);
		assertEquals(1, subscription.unboundedRequests.get());
		assertEquals(1, subscription.cancellations.get());
	}

	@Test
	public void collectorWaitsForDemandAndStartsSourceOnlyOnce() {
		TrackingObservable source = new TrackingObservable();
		AtomicReference<Subscription> downstreamSubscription = new AtomicReference<>();
		source.toList().subscribe(subscriberWithoutDemand(downstreamSubscription));

		assertEquals(0, source.subscription.unboundedRequests.get());

		downstreamSubscription.get().request(1);
		downstreamSubscription.get().request(2);
		downstreamSubscription.get().requestUnbounded();

		assertEquals(1, source.subscription.unboundedRequests.get());
	}

	@Test
	public void collectorForwardsCancellationBeforeDemand() {
		TrackingObservable source = new TrackingObservable();
		AtomicReference<Subscription> downstreamSubscription = new AtomicReference<>();
		source.toList().subscribe(subscriberWithoutDemand(downstreamSubscription));
		Unsubscription reason = new Unsubscription("test");

		downstreamSubscription.get().cancel(reason);
		downstreamSubscription.get().cancel();

		assertEquals(0, source.subscription.unboundedRequests.get());
		assertEquals(1, source.subscription.cancellations.get());
		assertSame(reason, source.subscription.unsubscription.get());
	}

	private static Subscriber<Object> subscriberWithoutDemand(AtomicReference<Subscription> subscription) {
		return new Subscriber<>() {
			@Override
			protected void onSubscribe(Subscription value) {
				subscription.set(value);
			}

			@Override
			protected void onNext(Object item) {
			}

			@Override
			protected void onComplete(Completion completion) {
			}
		};
	}

	private static final class TrackingObservable extends Observable<Integer> {

		private final TrackingSubscription subscription = new TrackingSubscription();

		@Override
		public void subscribe(Subscriber<? super Integer> subscriber) {
			subscriber.setSubscription(subscription);
		}

	}

	private static final class TrackingSubscription extends Subscription {

		private final AtomicInteger unboundedRequests = new AtomicInteger();
		private final AtomicInteger cancellations = new AtomicInteger();
		private final AtomicReference<Unsubscription> unsubscription = new AtomicReference<>();

		@Override
		protected void onRequest(long count) {
		}

		@Override
		protected void onUnboundRequest() {
			unboundedRequests.incrementAndGet();
		}

		@Override
		protected void onCancel(Unsubscription value) {
			cancellations.incrementAndGet();
			unsubscription.set(value);
		}

	}

}
