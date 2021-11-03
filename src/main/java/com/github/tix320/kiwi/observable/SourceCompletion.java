package com.github.tix320.kiwi.observable;

public non-sealed class SourceCompletion extends Completion {

	public static final SourceCompletion DEFAULT = new SourceCompletion();

	public SourceCompletion(Object data) {
		super(data);
	}

	public SourceCompletion() {
		this(null);
	}
}
