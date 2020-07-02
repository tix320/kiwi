package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.function.IntFunction;
import java.util.function.Predicate;

/**
 * @author Tigran Sargsyan on 11-Jun-20.
 */
public final class ArrayUtils {

	public static <T> T[] removeItem(T[] array, T item, IntFunction<T[]> newArrayFactory) {
		return removeIf(array, t -> t.equals(item), newArrayFactory);
	}

	public static <T> T[] removeIndex(T[] array, int index, IntFunction<T[]> newArrayFactory) {
		int len = array.length;

		T[] newArray = newArrayFactory.apply(len - 1);

		System.arraycopy(array, 0, newArray, 0, index);
		System.arraycopy(array, index + 1, newArray, index, len - index - 1);

		return newArray;
	}

	public static <T> T[] removeIf(T[] array, Predicate<T> finder, IntFunction<T[]> newArrayFactory) {
		int len = array.length;

		int index = -1;
		for (int i = 0; i < len; i++) {
			T o = array[i];
			if (finder.test(o)) {
				index = i;
			}
		}

		if (index == -1) {
			return array;
		}

		T[] newArray = newArrayFactory.apply(len - 1);

		System.arraycopy(array, 0, newArray, 0, index);
		System.arraycopy(array, index + 1, newArray, index, len - index - 1);

		return newArray;
	}

	public static <T> T[] addItem(T[] array, T item, IntFunction<T[]> newArrayFactory) {
		int len = array.length;
		T[] newArray = newArrayFactory.apply(len + 1);
		System.arraycopy(array, 0, newArray, 0, array.length);
		newArray[len] = item;
		return newArray;
	}

	public static <T> T[] changeItem(T[] array, int index, T item, IntFunction<T[]> newArrayFactory) {
		int len = array.length;
		T[] newArray = newArrayFactory.apply(len);
		System.arraycopy(array, 0, newArray, 0, array.length);
		newArray[index] = item;
		return newArray;
	}
}
