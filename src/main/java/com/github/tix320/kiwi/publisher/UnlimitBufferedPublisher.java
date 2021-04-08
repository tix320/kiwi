package com.github.tix320.kiwi.publisher;

public final class UnlimitBufferedPublisher<T> extends BufferedPublisher<T> {

	public UnlimitBufferedPublisher() {
		super(-1);
	}

	public UnlimitBufferedPublisher(Iterable<T> iterable) {
		super(-1);
		synchronized (this) {
			for (T t : iterable) {
				publish(t); // TODO batch publish
			}
		}
	}
}
