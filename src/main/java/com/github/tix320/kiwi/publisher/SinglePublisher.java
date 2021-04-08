package com.github.tix320.kiwi.publisher;


import java.util.Objects;

public class SinglePublisher<T> extends BufferedPublisher<T> {

	public SinglePublisher() {
		super(1);
	}

	public SinglePublisher(T initialValue) {
		super(1);
		synchronized (this) {
			publish(initialValue);
		}
	}

	public boolean CASPublish(T expected, T newValue) {
		Objects.requireNonNull(expected);
		Objects.requireNonNull(newValue);
		synchronized (this) {
			T lastItem = getValueAt(queueSize() - 1);
			if (lastItem.equals(expected)) {
				publish(newValue);
				return true;
			} else {
				return false;
			}
		}
	}

	public T getValue() {
		synchronized (this) {
			if (queueSize() == 0) {
				return null;
			}

			return getValueAt(queueSize() - 1);
		}
	}
}
