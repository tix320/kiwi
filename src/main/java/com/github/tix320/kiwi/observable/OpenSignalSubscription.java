package com.github.tix320.kiwi.observable;

import java.util.Optional;

import com.github.tix320.kiwi.publisher.Signal;

public interface OpenSignalSubscription extends Subscription {

	/**
	 * This is used for observables implementations. This should not be useful for user.
	 */
	Optional<Signal> currentSignal();
}
