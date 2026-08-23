package com.github.tix320.kiwi.observable.transform.multiple.internal;

import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import com.github.tix320.skimp.exception.ExceptionUtils;

final class SubscriptionUtils {

	private SubscriptionUtils() {
	}

	static void cancelAll(Iterable<Subscription> subscriptions, Unsubscription unsubscription) {
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

		if (failure != null) {
			ExceptionUtils.sneakyThrow(failure);
		}
	}

}
