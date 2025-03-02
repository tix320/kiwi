package com.github.tix320.kiwi.observable.signal;

import static com.github.tix320.kiwi.observable.signal.DefaultPriorities.COMPLETE_PRIORITY;

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
		return COMPLETE_PRIORITY;
	}

	@Override
	public final void accept(SignalVisitor signalVisitor) {
		signalVisitor.visit(this);
	}

}
