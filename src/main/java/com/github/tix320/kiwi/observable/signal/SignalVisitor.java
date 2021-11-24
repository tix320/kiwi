package com.github.tix320.kiwi.observable.signal;

public interface SignalVisitor<R> {

	R visit(PublishSignal<?> publishSignal);

	R visit(CancelSignal cancelSignal);

	R visit(CompleteSignal completeSignal);

	R visit(ErrorSignal errorSignal);
}
