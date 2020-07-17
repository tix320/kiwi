package com.github.tix320.kiwi.api.reactive.publisher;

public final class UnlimitBufferedPublisher<T> extends BufferedPublisher<T> {

	public UnlimitBufferedPublisher() {
		super(-1);
	}

	public UnlimitBufferedPublisher(Iterable<T> iterable) {
		super(-1);
		synchronized (this) {
			for (T t : iterable) {
				queue.add(t);
			}
		}
	}
}
