package com.github.tix320.kiwi.observable;

import com.github.tix320.kiwi.observable.Observable;

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
