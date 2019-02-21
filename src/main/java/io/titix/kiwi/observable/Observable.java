package io.titix.kiwi.observable;

import java.util.function.Consumer;

/**
 * @author tix32 on 21-Feb-19
 */
public interface Observable<T> {

	Subscription subscribe(Consumer<T> consumer);

	Observable<T> take(long count);

	Observable<T> one();
}
