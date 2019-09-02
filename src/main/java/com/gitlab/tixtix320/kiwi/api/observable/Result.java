package com.gitlab.tixtix320.kiwi.api.observable;

public final class Result<T> {

    private static final Object NULL = new Object();

    private static final Result<?> EMPTY = new Result<>(NULL, true);

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

    @SuppressWarnings("unchecked")
    public static <T> Result<T> empty() {
        return (Result<T>) EMPTY;
    }

    public boolean isPresent() {
        return this.value == NULL;
    }

    @SuppressWarnings("unchecked")
    public <R> Result<R> process(Processor<T> processor) {
        if (value == null) {
            return (Result<R>) this;
        }
        return processor.consume(value, hasNext);
    }

    public <R> Result<R> copy(R value, boolean hasNext) {
        return new Result<>(value, hasNext);
    }

    public <R> Result<R> changeValue(R value) {
        return new Result<>(value, hasNext);
    }

    public Result<T> copy(boolean hasNext) {
        return new Result<>(value, hasNext);
    }

    @SuppressWarnings("unchecked")
    public <R> Result<R> withoutValue() {
        return new Result<>((R) NULL, hasNext);
    }

    public interface Processor<T> {

        <R> Result<R> consume(T value, boolean hasNext);
    }
}
