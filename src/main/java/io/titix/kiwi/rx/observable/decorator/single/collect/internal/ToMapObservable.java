package io.titix.kiwi.rx.observable.decorator.single.collect.internal;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.titix.kiwi.rx.observable.internal.BaseObservable;

/**
 * @author tix32 on 27-Feb-19
 */
public final class ToMapObservable<T, K, V> extends CollectorObservable<T, Map<K, V>> {

	private final Function<? super T, ? extends K> keyMapper;

	private final Function<? super T, ? extends V> valueMapper;

	public ToMapObservable(BaseObservable<T> observable, Function<? super T, ? extends K> keyMapper,
						   Function<? super T, ? extends V> valueMapper) {
		super(observable);
		this.keyMapper = keyMapper;
		this.valueMapper = valueMapper;
	}

	@Override
	protected Map<K, V> collect(Stream<T> objects) {
		return objects.collect(Collectors.toMap(keyMapper, valueMapper));
	}
}
