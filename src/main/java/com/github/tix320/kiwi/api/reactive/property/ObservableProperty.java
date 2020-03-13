package com.github.tix320.kiwi.api.reactive.property;

import com.github.tix320.kiwi.api.reactive.observable.Observable;

public interface ObservableProperty<T> {

	T get();

	Observable<T> asObservable();
}
