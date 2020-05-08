package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Tigran Sargsyan on 03-May-20.
 */
public class ImprovedReentrantLock extends ReentrantLock {

	public ImprovedReentrantLock() {
	}

	public ImprovedReentrantLock(boolean fair) {
		super(fair);
	}

	@Override
	public Thread getOwner() {
		return super.getOwner();
	}
}
