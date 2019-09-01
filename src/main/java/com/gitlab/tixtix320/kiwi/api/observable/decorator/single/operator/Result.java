package com.gitlab.tixtix320.kiwi.api.observable.decorator.single.operator;

public final class Result<T> {

    private static final Object NULL = new Object();

    private static final Result<?> EMPTY = new Result<>(NULL, true);

    private static final Result<?> UNSUBSCRIBE = new Result<>(NULL, false);

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
    public static <T> Result<T> needWhen(boolean needMore) {
        return new Result<>((T) NULL, needMore);
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
        return this.value != NULL;
	}

	@Override
	public String toString() {
		return "Result{" + "value=" + value + ", needMore=" + needMore + '}';
	}
}
