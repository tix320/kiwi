package com.github.tix320.kiwi.publisher.internal;

import com.github.tix320.kiwi.observable.Unsubscription;

final class SubscriberUnsubscription {

	private final int lastItemIndex;

	private final Unsubscription unsubscription;

	private volatile boolean performed;

	SubscriberUnsubscription(int lastItemIndex, Unsubscription unsubscription) {
		this.lastItemIndex = lastItemIndex;
		this.unsubscription = unsubscription;
		this.performed = false;
	}

	public int lastItemIndex() {
		return lastItemIndex;
	}

	public Unsubscription unsubscription() {
		return unsubscription;
	}

	public boolean performed() {
		return performed;
	}

	public void setPerformed() {
		this.performed = true;
	}
}
