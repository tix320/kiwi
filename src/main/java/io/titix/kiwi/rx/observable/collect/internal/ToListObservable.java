package io.titix.kiwi.rx.observable.collect.internal;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.titix.kiwi.rx.observable.Observable;

/**
 * @author tix32 on 01-Mar-19
 */
public final class ToListObservable<T> extends CollectorObservable<T, List<T>> {

	public ToListObservable(Observable<T> observable) {
		super(observable);
	}

	@Override
	protected List<T> collect(Stream<T> objects) {
		return objects.collect(Collectors.toList());
	}
}
