package io.titix.kiwi.rx.internal.subject.single;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * @author tix32 on 23-Feb-19
 */
public final class ConcurrentSingleSubject<T> extends SingleSubject<T> {

	@Override
	protected Collection<Consumer<T>> container() {
		return new ConcurrentLinkedQueue<>();
	}
}
