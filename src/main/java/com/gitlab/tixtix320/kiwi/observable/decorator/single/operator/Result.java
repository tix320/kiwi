package com.gitlab.tixtix320.kiwi.observable.decorator.single.operator;

public final class Result<T> {

	private static final Result<?> EMPTY = new Result<>(null, true);

	private static final Result<?> UNSUBSCRIBE = new Result<>(null, false);

	private final T value;

	private final boolean needMore;

	private Result(T value, boolean needMore) {
		this.value = value;
		this.needMore = needMore;
	}

	public static <T> Result<T> of(T value) {
		return new Result<>(value, true);
	}

	public static <T> Result<T> lastOne(T value) {
		return new Result<>(value, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> Result<T> empty() {
		return (Result<T>) EMPTY;
	}

	@SuppressWarnings("unchecked")
	public static <T> Result<T> unsubscribe() {
		return (Result<T>) UNSUBSCRIBE;
	}

	public boolean isNeedMore() {
		return needMore;
	}

	public T getValue() {
		return value;
	}

	public boolean isPresent() {
		return this != EMPTY && this != UNSUBSCRIBE;
	}

	@Override
	public String toString() {
		return "Result{" + "value=" + value + ", needMore=" + needMore + '}';
	}
}
