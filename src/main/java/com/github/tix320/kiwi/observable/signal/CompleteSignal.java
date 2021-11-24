package com.github.tix320.kiwi.observable.signal;

import com.github.tix320.kiwi.observable.SourceCompletion;

public non-sealed class CompleteSignal extends Signal {

	protected final SourceCompletion completion;

	public CompleteSignal(SourceCompletion completion) {
		this.completion = completion;
	}

	public final SourceCompletion completion() {
		return completion;
	}

	@Override
	public int defaultPriority() {
		return PublishSignal.DEFAULT_PRIORITY;
	}

	@Override
	public final <R> R accept(SignalVisitor<R> signalVisitor) {
		return signalVisitor.visit(this);
	}
}
