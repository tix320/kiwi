package com.github.tix320.kiwi.observable.signal;

import static com.github.tix320.kiwi.observable.signal.SignalPriorities.CANCEL_PRIORITY;

import com.github.tix320.kiwi.observable.Unsubscription;
import java.util.Objects;

public non-sealed class CancelSignal extends Signal {

	protected final Unsubscription unsubscription;

	public CancelSignal(Unsubscription unsubscription) {
		this.unsubscription = Objects.requireNonNull(unsubscription, "Unsubscription must not be null");
	}

	public final Unsubscription unsubscription() {
		return unsubscription;
	}

	@Override
	public int priority() {
		return CANCEL_PRIORITY;
	}

}
