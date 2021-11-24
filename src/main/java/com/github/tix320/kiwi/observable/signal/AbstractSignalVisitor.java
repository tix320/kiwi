package com.github.tix320.kiwi.observable.signal;

public abstract class AbstractSignalVisitor<R> implements SignalVisitor<R> {

	@Override
	public R visit(PublishSignal<?> publishSignal) {
		throw new IllegalStateException();
	}

	@Override
	public R visit(CancelSignal cancelSignal) {
		throw new IllegalStateException();
	}

	@Override
	public R visit(CompleteSignal completeSignal) {
		throw new IllegalStateException();
	}

	@Override
	public R visit(ErrorSignal errorSignal) {
		throw new IllegalStateException();
	}
}
