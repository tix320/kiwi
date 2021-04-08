package com.github.tix320.kiwi.observable;

/**
 * Marker interface, which indicates that one item will be obtained from it, after which it will be automatically unsubscribed.
 *
 * @param <T> type of items
 */
public interface MonoObservable<T> extends Observable<T> {}
