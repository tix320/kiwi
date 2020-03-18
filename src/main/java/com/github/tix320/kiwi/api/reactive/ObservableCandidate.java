package com.github.tix320.kiwi.api.reactive;

import com.github.tix320.kiwi.api.reactive.observable.Observable;

public interface ObservableCandidate<T> {

	/**
	 * Convert current to object, which can be regularly publish items.
	 *
	 * @return created observable
	 */
	Observable<T> asObservable();
}
