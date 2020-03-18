package com.github.tix320.kiwi.api.reactive.stock;

import java.util.List;

import com.github.tix320.kiwi.api.reactive.ObservableCandidate;

public interface ObservableStock<T> extends ObservableCandidate<T> {

	List<T> list();
}
