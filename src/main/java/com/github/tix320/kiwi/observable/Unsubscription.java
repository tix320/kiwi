package com.github.tix320.kiwi.observable;

public non-sealed class Unsubscription extends Completion {

	public static final Unsubscription DEFAULT = new Unsubscription();

	public Unsubscription(Object data) {
		super(data);
	}

	public Unsubscription() {
		this(null);
	}
}
