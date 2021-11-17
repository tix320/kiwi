package com.github.tix320.kiwi.observable.signal;

public interface SignalVisitor {

	SignalVisitResult visit(NextSignal<?> nextSignal);

	SignalVisitResult visit(CancelSignal cancelSignal);

	SignalVisitResult visit(CompleteSignal completeSignal);

	SignalVisitResult visit(ErrorSignal errorSignal);

	enum SignalVisitResult {
		CONTINUE, REQUEUE_AND_PAUSE, COMPLETE
	}
}
