package io.titix.kiwi.rx.internal.subject.single;

import java.util.Collection;
import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * @author tix32 on 23-Feb-19
 */
public final class SimpleSingleSubject<T> extends SingleSubject<T> {

	@Override
	protected Collection<Consumer<T>> container() {
		return new LinkedList<>();
	}
}
