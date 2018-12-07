package de.fsyo.uremn.check;

final class Success<T> implements Try<T> {

	static final Success<?> EMPTY = new Success<>(null);

	private final T value;

	Success(T value) {
		this.value = value;
	}

	@Override
	public boolean isSuccess() {
		return true;
	}

	@Override
	public boolean isFailure() {
		return false;
	}

	@Override
	public T get() {
		return value;
	}
}
