package com.github.tix320.kiwi.api.reactive;

import com.github.tix320.kiwi.api.reactive.observable.Observable;

public interface ObservableCandidate<T> {

	/**
	 * Convert current to special object (Observable), which can be regularly produce items.
	 *
	 * @return created observable
	 *
	 * @see Observable
	 */
	Observable<T> asObservable();
}
