package com.github.tix320.kiwi.observable.signal;

import static com.github.tix320.kiwi.observable.signal.SignalPriorities.COMPLETE_PRIORITY;

import com.github.tix320.kiwi.observable.SourceCompletion;
import java.util.Objects;

public non-sealed class CompleteSignal extends Signal {

	protected final SourceCompletion completion;

	public CompleteSignal(SourceCompletion completion) {
		this.completion = Objects.requireNonNull(completion, "Source completion must not be null");
	}

	public final SourceCompletion completion() {
		return completion;
	}

	@Override
	public int priority() {
		return COMPLETE_PRIORITY;
	}

}
