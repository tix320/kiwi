package com.github.tix320.kiwi.api.reactive.stock;

import java.util.List;

import com.github.tix320.kiwi.api.reactive.observable.Observable;

public interface ObservableStock<T> {

	List<T> list();

	Observable<T> asObservable();
}
