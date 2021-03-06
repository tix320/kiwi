package com.github.tix320.kiwi.property;

import java.util.List;

import com.github.tix320.kiwi.observable.ObservableCandidate;

public interface ObservableStock<T> extends ObservableCandidate<T> {

	List<T> list();
}
