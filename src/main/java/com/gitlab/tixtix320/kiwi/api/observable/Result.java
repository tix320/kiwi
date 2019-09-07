package com.gitlab.tixtix320.kiwi.api.observable;

public final class Result<T> {

    private final T value;

    private final boolean hasNext;

    private Result(T value, boolean hasNext) {
        this.value = value;
        this.hasNext = hasNext;
    }

    public static <T> Result<T> of(T value) {
        return new Result<>(value, true);
    }

    public static <T> Result<T> of(T value, boolean hasNext) {
        return new Result<>(value, hasNext);
    }

    public static <T> Result<T> lastOne(T value) {
        return new Result<>(value, false);
    }

    public <R> Result<R> changeValue(R value) {
        return new Result<>(value, hasNext);
    }

    public Result<T> copy(boolean hasNext) {
        return new Result<>(value, hasNext);
    }

    public T getValue() {
        return value;
    }

    public boolean hasNext() {
        return hasNext;
    }
}
