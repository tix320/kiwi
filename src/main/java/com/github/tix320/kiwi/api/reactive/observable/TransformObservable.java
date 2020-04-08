package com.github.tix320.kiwi.api.reactive.observable;

/**
 * An interface for such observables, that transform the source elements to another.
 *
 * @param <S> type of source items
 * @param <R> type of produced items
 */
public interface TransformObservable<S, R> extends Observable<R> {}
