package com.github.tix320.kiwi.observable.signal;

import static com.github.tix320.kiwi.observable.signal.DefaultPriorities.CANCEL_PRIORITY;

import com.github.tix320.kiwi.observable.Unsubscription;

public non-sealed class CancelSignal extends Signal {

	protected final Unsubscription unsubscription;

	public CancelSignal(Unsubscription unsubscription) {
		this.unsubscription = unsubscription;
	}

	public final Unsubscription unsubscription() {
		return unsubscription;
	}

	@Override
	public int defaultPriority() {
		return CANCEL_PRIORITY;
	}

	@Override
	public final void accept(SignalVisitor signalVisitor) {
		signalVisitor.visit(this);
	}

}
