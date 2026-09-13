package com.github.tix320.kiwi.observable.transform.multiple.internal;

import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import com.github.tix320.skimp.exception.ExceptionUtils;

final class SubscriptionUtils {

	private SubscriptionUtils() {
	}

	static void requestAll(Iterable<Subscription> subscriptions, long count) {
		requestAll(subscriptions, subscription -> subscription.request(count));
	}

	static void requestUnboundedAll(Iterable<Subscription> subscriptions) {
		requestAll(subscriptions, Subscription::requestUnbounded);
	}

	static void cancelAll(Iterable<Subscription> subscriptions, Unsubscription unsubscription) {
		Exception failure = cancelAllAndGetFailure(subscriptions, unsubscription);
		if (failure != null) {
			ExceptionUtils.sneakyThrow(failure);
		}
	}

	static void cancelAllThen(Iterable<Subscription> subscriptions, Unsubscription unsubscription, Runnable action) {
		Exception failure = cancelAllAndGetFailure(subscriptions, unsubscription);
		try {
			action.run();
		} catch (Exception actionFailure) {
			if (failure != null) {
				actionFailure.addSuppressed(failure);
			}
			failure = actionFailure;
		}

		if (failure != null) {
			ExceptionUtils.sneakyThrow(failure);
		}
	}

	private static Exception cancelAllAndGetFailure(
		Iterable<Subscription> subscriptions, Unsubscription unsubscription) {
		Exception failure = null;
		for (Subscription subscription : subscriptions) {
			try {
				subscription.cancel(unsubscription);
			} catch (Exception exception) {
				if (failure == null) {
					failure = exception;
				} else {
					failure.addSuppressed(exception);
				}
			}
		}

		return failure;
	}

	static void rollback(Iterable<Subscription> subscriptions, Exception subscriptionFailure) {
		try {
			cancelAll(subscriptions, new Unsubscription("MULTI_SOURCE_SUBSCRIPTION_FAILED"));
		} catch (Exception cancellationFailure) {
			subscriptionFailure.addSuppressed(cancellationFailure);
		}
		ExceptionUtils.sneakyThrow(subscriptionFailure);
	}

	private static void requestAll(Iterable<Subscription> subscriptions, RequestAction action) {
		Exception failure = null;
		for (Subscription subscription : subscriptions) {
			try {
				action.request(subscription);
			} catch (Exception exception) {
				if (failure == null) {
					failure = exception;
				} else {
					failure.addSuppressed(exception);
				}
			}
		}

		if (failure != null) {
			ExceptionUtils.sneakyThrow(failure);
		}
	}

	@FunctionalInterface
	private interface RequestAction {

		void request(Subscription subscription);

	}

}
