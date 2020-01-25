package com.github.tix320.kiwi.api.util;

/**
 * This class may used to avoid a null values, passing anywhere singleton object {@link #SELF}.
 */
public final class None {
	public static final None SELF = new None();

	private None() {
		if (SELF != null) {
			throw new IllegalStateException("Cannot be instantiated");
		}
	}
}

