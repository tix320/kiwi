package com.github.tix320.kiwi.observable;

import com.github.tix320.kiwi.observable.signal.SignalManager;
import com.github.tix320.skimp.api.exception.ExceptionUtils;

/**
 * @author : Tigran Sargsyan
 * @since : 15.08.2021
 **/
public abstract class FlexibleSubscriber<T> extends Subscriber<T> {

	protected FlexibleSubscriber(SignalManager signalManager) {
		super(signalManager);
	}

	protected FlexibleSubscriber() {
		super(new SignalManager(1));
	}

	@Override
	public void onSubscribe(Subscription subscription) {
		subscription.requestUnbounded();
	}

	@Override
	public void onNext(T item) {
		// No-op
	}

	@Override
	protected void onError(Throwable error) {
		ExceptionUtils.applyToUncaughtExceptionHandler(new UnhandledObservableException(error));
	}

	@Override
	public void onComplete(Completion completion) {
		// No-op
	}

	private static final class UnhandledObservableException extends RuntimeException {
		public UnhandledObservableException(Throwable cause) {
			super(cause);
		}
	}
}
