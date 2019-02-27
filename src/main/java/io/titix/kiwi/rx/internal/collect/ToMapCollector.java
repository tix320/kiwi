package io.titix.kiwi.rx.internal.collect;

import java.util.function.Consumer;
import java.util.function.Function;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.observer.Manager;
import io.titix.kiwi.rx.internal.subject.BaseSubject;
import io.titix.kiwi.rx.internal.subject.BufferSubject;

/**
 * @author tix32 on 27-Feb-19
 */
public class ToMapCollector<T, K, V> extends BaseSubject<T>, implements Observable<T> {

	private final BaseSubject<T> subject;

	private final Function<? super T, ? extends K> keyMapper;

	private final Function<? super T, ? extends V> valueMapper;

	ToMapCollector(Observable<T> observable, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		this.subject = new BufferSubject<>(1);
		this.keyMapper = keyMapper;
		this.valueMapper = valueMapper;
	}

	@Override
	public Subscription subscribe(Consumer<? super T> consumer) {
		return observers.add();
	}
}
