package io.titix.kiwi.rx.internal.observer;

import java.util.function.Consumer;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public interface Manager<T> {

	void add(Consumer<? super T> consumer);

	void remove(Consumer<? super T> consumer);
}
