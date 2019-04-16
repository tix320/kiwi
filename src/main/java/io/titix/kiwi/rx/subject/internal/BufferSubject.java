package io.titix.kiwi.rx.subject.internal;

import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

import io.titix.kiwi.rx.observable.Observer;
import io.titix.kiwi.rx.observable.ObserverWithSubscription;
import io.titix.kiwi.rx.observable.Subscription;

/**
 * @author tix32 on 21-Feb-19
 */
public final class BufferSubject<T> extends BaseSubject<T> {

	private final Deque<T> buffer;

	private final long bufferSize;

	public BufferSubject(int bufferSize) {
		buffer = new ConcurrentLinkedDeque<>();
		this.bufferSize = bufferSize < 0 ? 0 : bufferSize;
	}

	@Override
	public Subscription addObserver(Observer<? super T> observer) {
		observers.add(observer);
		buffer.forEach(observer::consume);
		return () -> observers.remove(observer);
	}

	@Override
	public Subscription addObserver(ObserverWithSubscription<? super T> observer) {
		Observer<T> realObserver = new Observer<>() {
			@Override
			public void consume(T object) {
				observer.consume(object, () -> observers.remove(this));
			}
		};
		observers.add(realObserver);
		for (T object : buffer) {
			realObserver.consume(object);
			if (!observers.contains(realObserver)) {
				break;
			}
		}
		return () -> observers.remove(realObserver);
	}

	@Override
	protected void preNext(T object) {
		if (buffer.size() == bufferSize) {
			buffer.removeFirst();
		}
		buffer.addLast(object);
	}
}
