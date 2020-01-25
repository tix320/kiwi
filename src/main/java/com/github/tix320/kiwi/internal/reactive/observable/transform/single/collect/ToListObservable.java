package com.github.tix320.kiwi.internal.reactive.observable.transform.single.collect;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.tix320.kiwi.api.reactive.observable.Observable;

/**
 * @author Tigran Sargsyan on 01-Mar-19
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
