package de.fsyo.uremn.check;

final class Failure<C extends Throwable> implements Try<C> {
	
	private final C cause;

	public Failure(C cause) {
		this.cause = cause;
	}

	@Override
	public boolean isSuccess() {
		return false;
	}

	@Override
	public boolean isFailure() {
		return true;
	}

	@Override
	public C get() {
		return cause;
	}
}
