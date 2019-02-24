package io.titix.kiwi.rx.internal.subject;

import java.util.Collection;
import java.util.LinkedList;

/**
 * @author tix32 on 23-Feb-19
 */
public final class SimpleSingleSubject<T> extends SingleSubject<T> {

	@Override
	Collection<Observer<T>> container() {
		return new LinkedList<>();
	}
}
