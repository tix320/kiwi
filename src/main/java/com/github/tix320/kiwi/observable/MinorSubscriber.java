package com.github.tix320.kiwi.observable;

import com.github.tix320.skimp.exception.ExceptionUtils;

/**
 * @author Tigran Sargsyan on 25.02.25
 */
public abstract class MinorSubscriber<T, P> {

	private Subscriber<? super P> parent;
	private Subscriber<? extends T> delegate;

	protected abstract void onSubscribe(Subscription subscription);

	protected abstract void onNext(T item);

	protected abstract void onComplete(Completion completion);

	protected void onError(Throwable error) {
		parent.completeWithError(error);
	}

	void setDelegate(Subscriber<? extends T> delegate) {
		this.delegate = delegate;
	}

	void setParent(Subscriber<? super P> parent) {
		this.parent = parent;
	}

	protected final Subscriber<? super P> parent() {
		return parent;
	}

	protected final Subscription subscription() {
		return delegate.subscription();
	}

	protected final void replaceDemandAndRethrow(Exception failure) {
		try {
			subscription().request(1);
		} catch (Exception requestFailure) {
			failure.addSuppressed(requestFailure);
		}
		ExceptionUtils.sneakyThrow(failure);
	}

}
