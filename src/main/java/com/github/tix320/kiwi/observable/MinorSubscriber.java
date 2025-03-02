package com.github.tix320.kiwi.observable;

/**
 * @author Tigran Sargsyan on 25.02.25
 */
public abstract class MinorSubscriber<T, P> {

	private Subscriber<? super P> parent;
	private Subscriber<? extends T> delegate;

	protected abstract void onSubscribe(Subscription subscription);

	protected abstract void onNext(T item);

	protected abstract void onComplete(Completion completion);

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

}
