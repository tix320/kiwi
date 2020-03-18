package com.github.tix320.kiwi.api.reactive.property;

import com.github.tix320.kiwi.api.reactive.ObservableCandidate;

public interface ObservableProperty<T> extends ObservableCandidate<T> {

	T get();
}
