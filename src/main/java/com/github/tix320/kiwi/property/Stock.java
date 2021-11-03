package com.github.tix320.kiwi.property;

import java.util.List;

import com.github.tix320.kiwi.observable.ObservableCandidate;

public interface Stock<T> extends ObservableCandidate<T> {

	List<T> list();

	// ---------- Factory methods ----------

	static <T> ObjectStock<T> forObject() {
		return new ObjectStock<>();
	}
}
