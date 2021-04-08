package com.github.tix320.kiwi.property;


import com.github.tix320.kiwi.observable.ObservableCandidate;

public interface ObservableProperty<T> extends ObservableCandidate<T> {

	T getValue();
}
