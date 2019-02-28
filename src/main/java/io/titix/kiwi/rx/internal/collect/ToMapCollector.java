package io.titix.kiwi.rx.internal.collect;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subscription;
import io.titix.kiwi.rx.internal.subject.BaseSubject;

/**
 * @author tix32 on 27-Feb-19
 */
public class ToMapCollector<T, K, V> extends BaseSubject<T> implements Observable<Map<K, V>> {

	private final Queue<T> objects;

	private final Function<? super T, ? extends K> keyMapper;

	private final Function<? super T, ? extends V> valueMapper;

	public ToMapCollector(Observable<T> observable, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
		this.objects = new ConcurrentLinkedQueue<>();
		this.keyMapper = keyMapper;
		this.valueMapper = valueMapper;
		observable.subscribe(this::next);
	}

	@Override
	protected void preNext(T object) {
		objects.add(object);
	}

	@Override
	public Subscription subscribe(Consumer<? super Map<K, V>> consumer) {
		return onComplete(() -> consumer.accept(objects.stream().collect(Collectors.toMap(keyMapper, valueMapper))));
	}

	@Override
	protected boolean addObserver(Consumer<? super T> consumer) {
		return true;
	}
}
