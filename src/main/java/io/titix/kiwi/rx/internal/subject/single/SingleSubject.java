package io.titix.kiwi.rx.internal.subject.single;

import java.util.function.Consumer;

import io.titix.kiwi.rx.internal.observer.ObserverManager;
import io.titix.kiwi.rx.internal.subject.BaseSubject;

/**
 * @author tix32 on 21-Feb-19
 */
public abstract class SingleSubject<T> extends BaseSubject<T> {

	@Override
	public ObserverManager<T> manager() {
		return new ObserverManagerImpl();
	}

	private final class ObserverManagerImpl implements ObserverManager<T> {

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
