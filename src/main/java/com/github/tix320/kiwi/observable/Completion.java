package com.github.tix320.kiwi.observable;

public abstract sealed class Completion permits SourceCompleted, Unsubscription {

	private final Object data;

	public Completion(Object data) {
		this.data = data;
	}

	public <T> T data() {
		//noinspection unchecked
		return (T) data;
	}

	@Override
	public String toString() {
		return "%s[%s]".formatted(getClass().getSimpleName(), data);
	}
}
