package io.titix.kiwi.rx.internal.subject;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author tix32 on 23-Feb-19
 */
public final class ConcurrentSingleSubject<T> extends SingleSubject<T> {

	@Override
	Collection<Observer<T>> container() {
		return new ConcurrentLinkedQueue<>();
	}
}
