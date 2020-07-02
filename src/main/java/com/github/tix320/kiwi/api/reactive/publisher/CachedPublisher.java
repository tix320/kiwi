package com.github.tix320.kiwi.api.reactive.publisher;

public final class CachedPublisher<T> extends BufferedPublisher<T> {

	public CachedPublisher() {
		super(-1);
	}

	public CachedPublisher(Iterable<T> iterable) {
		this();
		for (T value : iterable) {
			buffer.add(value);
		}
	}

	@Override
	protected void addToBuffer(T item) {
		buffer.addLast(item);
	}
}
