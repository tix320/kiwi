package com.github.tix320.kiwi.api.util.collection;

import java.util.Map;

public interface BiMap<T1, T2> {

	void put(T1 key, T2 value);

	T2 straightRemove(T1 key);

	T1 inverseRemove(T2 key);

	Map<T1, T2> straightView();

	Map<T2, T1> inverseView();
}
