package com.github.tix320.kiwi.observable.signal;

import com.github.tix320.kiwi.observable.Unsubscription;

public non-sealed class CancelSignal extends Signal {

	public static final int DEFAULT_PRIORITY = 150;

	protected final Unsubscription unsubscription;

	public CancelSignal(Unsubscription unsubscription) {
		this.unsubscription = unsubscription;
	}

	public final Unsubscription unsubscription() {
		return unsubscription;
	}

	@Override
	public int defaultPriority() {
		return DEFAULT_PRIORITY;
	}

	@Override
	public final SignalVisitor.SignalVisitResult accept(SignalVisitor signalVisitor) {
		return signalVisitor.visit(this);
	}
}
