package com.github.tix320.kiwi.api.util;

/**
 * @author Tigran Sargsyan on 28-Mar-20.
 */
public class CantorPair {

	private final long first;

	private final long second;

	public CantorPair(long first, long second) {
		this.first = first;
		this.second = second;
	}

	public long first() {
		return first;
	}

	public long second() {
		return second;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		CantorPair that = (CantorPair) o;
		return first == that.first && second == that.second;
	}

	@Override
	public int hashCode() {
		long sum = first + second;
		long cantorResult = ((sum * (sum + 1)) / 2) + second;
		return (int) cantorResult;
	}
}
