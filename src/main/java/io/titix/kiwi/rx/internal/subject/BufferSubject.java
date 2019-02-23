package io.titix.kiwi.rx.internal.subject;

import java.util.Deque;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.observer.BaseObservable;
import io.titix.kiwi.rx.internal.observer.Observer;

/**
 * @author tix32 on 21-Feb-19
 */
abstract class BufferSubject<T> extends BaseSubject<T> {

	private final Deque<T> buffer;

	private final long bufferSize;

	BufferSubject(int bufferSize) {
		buffer = buffer();
		this.bufferSize = bufferSize < 1 ? 1 : bufferSize;
	}

	@Override
	public void preNext(T object) {
		if (buffer.size() == bufferSize) {
			buffer.removeFirst();
		}
		buffer.addLast(object);
	}

	@Override
	public Observable<T> asObservable() {
		return new BaseObservable<>() {

			@Override
			public Subscription subscribe(Consumer<T> consumer, BooleanSupplier predicate) {
				Observer<T> observer = new Observer<>(consumer, predicate);
				observers.add(observer);
				fillFromBuffer(observer);
				return () -> observers.remove(observer);
			}

			@Override
			public Subscription subscribe(Consumer<T> consumer) {
				Observer<T> observer = new Observer<>(consumer, () -> true);
				observers.add(observer);
				fillFromBuffer(observer);
				return () -> observers.remove(observer);
			}
		};
	}

	private void fillFromBuffer(Observer<T> observer) {
		for (T object : buffer) {
			if (observer.needMore()) {
				observer.next(object);
			}
			else {
				observers.remove(observer);
				break;
			}
		}
	}

	abstract Deque<T> buffer();
}
