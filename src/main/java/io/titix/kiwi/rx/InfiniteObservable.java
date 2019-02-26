package io.titix.kiwi.rx;

/**
 * @author Tigran.Sargsyan on 26-Feb-19
 */
public interface InfiniteObservable<T> extends Observable<T> {

	void stop();
}
