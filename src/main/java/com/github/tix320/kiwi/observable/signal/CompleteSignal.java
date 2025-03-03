package com.github.tix320.kiwi.observable.signal;

import static com.github.tix320.kiwi.observable.signal.SignalPriorities.COMPLETE_PRIORITY;

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
	public int priority() {
		return COMPLETE_PRIORITY;
	}

}
