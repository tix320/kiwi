package com.github.tix320.kiwi.property;

import com.github.tix320.kiwi.property.internal.AbstractMutableProperty;
import com.github.tix320.kiwi.publisher.SinglePublisher;
import java.util.Collection;
import java.util.Set;

/**
 * @author Tigran Sargsyan on 24-Mar-20.
 */
public final class SetProperty<T> extends AbstractMutableProperty<Set<T>> {

	public SetProperty(Set<T> set) {
		super(set);
	}

	public boolean contains(T key) {
		return getValue().contains(key);
	}

	public boolean add(T value) {
		return Boolean.TRUE.equals(publisher.modifyValue(set -> {
			var changed = set.add(value);
			return new SinglePublisher.ModifyResult<>(changed, changed);
		}));
	}

	public boolean addAll(Collection<? extends T> values) {
		return Boolean.TRUE.equals(publisher.modifyValue(set -> {
			var changed = set.addAll(values);
			return new SinglePublisher.ModifyResult<>(changed, changed);
		}));
	}

	public boolean remove(T key) {
		return Boolean.TRUE.equals(publisher.modifyValue(set -> {
			var changed = set.remove(key);
			return new SinglePublisher.ModifyResult<>(changed, changed);
		}));
	}

	public void clear() {
		publisher.modifyValue(set -> {
			set.clear();
			return new SinglePublisher.ModifyResult<>(null, true);
		});
	}

	@Override
	public String toString() {
		return "SetProperty{" + getValue() + "}";
	}

}
