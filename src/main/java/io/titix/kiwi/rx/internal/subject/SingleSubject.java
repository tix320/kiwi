package io.titix.kiwi.rx.internal.subject;

import java.util.function.Consumer;

/**
 * @author tix32 on 21-Feb-19
 */
public final class SingleSubject<T> extends BaseSubject<T> {

	@Override
	public Consumer<? super T> filterObserver(Consumer<? super T> consumer) {
		return consumer;
	}

	@Override
	protected void preNext(T object) {
	}
}
