package com.github.tix320.kiwi.observable.transform.single.collect.internal;

import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.tix320.kiwi.observable.Observable;

/**
 * @author Tigran.Sargsyan on 01-Mar-19
 */
public final class JoinObservable<T> extends CollectorObservable<T, String> {

	private final Function<? super T, ? extends String> toString;

	private final String prefix;

	private final String suffix;

	private final String delimiter;

	public JoinObservable(Observable<T> observable, Function<? super T, ? extends String> toString, String delimiter) {
		this(observable, toString, delimiter, "", "");
	}

	public JoinObservable(Observable<T> observable, Function<? super T, ? extends String> toString, String delimiter,
						  String prefix, String suffix) {
		super(observable);
		this.toString = toString;
		this.delimiter = delimiter;
		this.suffix = suffix;
		this.prefix = prefix;
	}

	@Override
	protected String collect(Stream<T> objects) {
		return objects.map(toString).collect(Collectors.joining(delimiter, prefix, suffix));
	}
}
