package com.github.tix320.kiwi.api.util;

/**
 * This class may used instead of null, passing singleton object {@link #SELF}.
 */
public final class None {
	public static final None SELF = new None();

	private None() {
		if (SELF != null) {
			throw new IllegalStateException("Cannot be instantiated");
		}
	}
}

