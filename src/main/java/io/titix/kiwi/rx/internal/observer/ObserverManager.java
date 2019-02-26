package io.titix.kiwi.rx.internal.observer;

import java.util.function.Consumer;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public interface ObserverManager<T> {

	void add(Consumer<T> consumer);

	void remove(Consumer<T> consumer);
}
