package com.github.tix320.kiwi.observable;

public non-sealed class SourceCompleted extends Completion {

	public static final SourceCompleted DEFAULT = new SourceCompleted();

	public SourceCompleted(Object data) {
		super(data);
	}

	public SourceCompleted() {
		this(null);
	}
}
