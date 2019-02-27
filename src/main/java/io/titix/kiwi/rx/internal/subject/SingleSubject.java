package io.titix.kiwi.rx.internal.subject;

import java.util.function.Consumer;

import io.titix.kiwi.rx.internal.observer.Manager;

/**
 * @author tix32 on 21-Feb-19
 */
public final class SingleSubject<T> extends BaseSubject<T> {

	@Override
	public Manager<T> manager() {
		return new ManagerImpl();
	}

	private final class ManagerImpl implements Manager<T> {

		@Override
		public void add(Consumer<T> consumer) {
			observers.add(consumer);
		}

		@Override
		public void remove(Consumer<T> consumer) {
			observers.remove(consumer);
		}
	}
}
