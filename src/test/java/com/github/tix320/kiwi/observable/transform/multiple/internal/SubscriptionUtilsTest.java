package com.github.tix320.kiwi.observable.transform.multiple.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

public class SubscriptionUtilsTest {

	@Test
	public void finiteRequestAttemptsEverySubscriptionAndAggregatesFailures() {
		RuntimeException firstFailure = new RuntimeException("first");
		RuntimeException secondFailure = new RuntimeException("second");
		AtomicLong successfulRequest = new AtomicLong();

		RuntimeException actual = assertThrows(RuntimeException.class,
			() -> SubscriptionUtils.requestAll(List.of(
				new TestSubscription(count -> {
					throw firstFailure;
				}),
				new TestSubscription(successfulRequest::addAndGet),
				new TestSubscription(count -> {
					throw secondFailure;
				})), 3));

		assertSame(firstFailure, actual);
		assertEquals(3, successfulRequest.get());
		assertEquals(List.of(secondFailure), List.of(actual.getSuppressed()));
	}

	@Test
	public void unboundedRequestAttemptsEverySubscription() {
		RuntimeException failure = new RuntimeException("failed");
		AtomicLong successfulRequests = new AtomicLong();

		RuntimeException actual = assertThrows(RuntimeException.class,
			() -> SubscriptionUtils.requestUnboundedAll(List.of(
				new TestSubscription(count -> {
					throw failure;
				}),
				new TestSubscription(successfulRequests::addAndGet))));

		assertSame(failure, actual);
		assertEquals(1, successfulRequests.get());
	}

	@Test
	public void cancellationFailureDoesNotPreventFollowingAction() {
		RuntimeException failure = new RuntimeException("failed");
		AtomicBoolean actionCalled = new AtomicBoolean(false);

		RuntimeException actual = assertThrows(RuntimeException.class,
			() -> SubscriptionUtils.cancelAllThen(List.of(new TestSubscription(count -> {
			}) {
				@Override
				protected void onCancel(Unsubscription unsubscription) {
					throw failure;
				}
			}), Unsubscription.DEFAULT, () -> actionCalled.set(true)));

		assertSame(failure, actual);
		assertTrue(actionCalled.get());
	}

	private static class TestSubscription extends Subscription {

		private final Request request;

		private TestSubscription(Request request) {
			this.request = request;
		}

		@Override
		protected void onRequest(long count) {
			request.apply(count);
		}

		@Override
		protected void onUnboundRequest() {
			request.apply(1);
		}

		@Override
		protected void onCancel(Unsubscription unsubscription) {
		}

	}

	@FunctionalInterface
	private interface Request {

		void apply(long count);

	}

}
