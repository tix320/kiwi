package com.github.tix320.kiwi.internal.observable.subject;

import java.util.Iterator;

import com.github.tix320.kiwi.api.observable.ConditionalConsumer;
import com.github.tix320.kiwi.api.observable.Result;
import com.github.tix320.kiwi.api.observable.Subscription;

/**
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class SimpleSubject<T> extends BaseSubject<T> {

	public void next(T object) {
		checkCompleted();
		Iterator<Observer<? super T>> iterator = observers.iterator();
		while (iterator.hasNext()) {
			Observer<? super T> observer = iterator.next();
			boolean needMore = observer.consume(object, !completed.get());
			if (!needMore) {
				iterator.remove();
			}
		}
	}

	@Override
	public void next(T[] objects) {
		checkCompleted();
		Iterator<Observer<? super T>> iterator = observers.iterator();
		while (iterator.hasNext()) {
			Observer<? super T> observer = iterator.next();
			for (T object : objects) {
				boolean needMore = observer.consume(object, !completed.get());
				if (!needMore) {
					iterator.remove();
					break;
				}
			}
		}
	}

	@Override
	public void next(Iterable<T> iterable) {
		checkCompleted();
		Iterator<Observer<? super T>> iterator = observers.iterator();
		while (iterator.hasNext()) {
			Observer<? super T> observer = iterator.next();
			for (T object : iterable) {
				boolean needMore = observer.consume(object, !completed.get());
				if (!needMore) {
					iterator.remove();
					break;
				}
			}
		}
	}

	@Override
	protected Subscription subscribe(ConditionalConsumer<? super Result<? extends T>> consumer) {
		Observer<T> observer = new Observer<>(consumer);
		observers.add(observer);
		return () -> observers.remove(observer);
	}
}
