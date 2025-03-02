package com.github.tix320.kiwi.observable.signal;

public interface SignalVisitor {

	void visit(PublishSignal<?> publishSignal);

	void visit(CompleteSignal completeSignal);

	void visit(ErrorSignal errorSignal);

	void visit(CancelSignal cancelSignal);

}
