package com.gitlab.tixtix320.kiwi.observable.decorator.single.reduce.collect.internal;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.gitlab.tixtix320.kiwi.observable.internal.BaseObservable;

/**
 * @author Tigran Sargsyan on 01-Mar-19
 */
public final class ToListObservable<T> extends CollectorObservable<T, List<T>> {

	public ToListObservable(BaseObservable<T> observable) {
		super(observable);
	}

	@Override
	protected List<T> collect(Stream<T> objects) {
		return objects.collect(Collectors.toList());
	}
}
