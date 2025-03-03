package com.github.tix320.kiwi.property;

import com.github.tix320.kiwi.property.internal.AbstractMutableProperty;
import com.github.tix320.kiwi.publisher.SinglePublisher;
import java.util.Collection;
import java.util.List;

public final class ListProperty<T> extends AbstractMutableProperty<List<T>> {

	public ListProperty(List<T> list) {
		super(list);
	}

	public int size() {
		return getValue().size();
	}

	public boolean isEmpty() {
		return getValue().isEmpty();
	}

	public T get(int index) {
		return getValue().get(index);
	}

	public void add(T value) {
		publisher.modifyValue(list -> {
			list.add(value);
			return new SinglePublisher.ModifyResult<>(null, true);
		});
	}

	public void addAll(Collection<? extends T> values) {
		publisher.modifyValue(list -> {
			list.addAll(values);
			return new SinglePublisher.ModifyResult<>(null, true);
		});
	}

	public T set(int index, T element) {
		return publisher.modifyValue(list -> {
			var prevValue = list.set(index, element);
			return new SinglePublisher.ModifyResult<>(prevValue, true);
		});
	}

	public void clear() {
		publisher.modifyValue(list -> {
			list.clear();
			return new SinglePublisher.ModifyResult<>(null, true);
		});
	}

	@Override
	public String toString() {
		return "ListProperty{" + getValue() + "}";
	}

}
