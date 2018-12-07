package de.fsyo.uremn.check;

final class Failure<F extends Throwable> implements Try<F>{
	
	private final F cause;

	public Failure(F cause) {
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
	public F get() {
		return cause;
	}
}
