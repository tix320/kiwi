package com.github.tix320.kiwi.publisher.internal.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class SlidingWindowLinkedList<E> {

	// VarHandle mechanics
	private static final VarHandle HEAD_HANDLE;
	private static final VarHandle TAIL_HANDLE;
	private static final VarHandle NEXT_HANDLE;
	private static final VarHandle CONCURRENCY_FLAG_HANDLE;

	static {
		try {
			MethodHandles.Lookup l = MethodHandles.lookup();
			HEAD_HANDLE = l.findVarHandle(SlidingWindowLinkedList.class, "_head", Node.class);
			TAIL_HANDLE = l.findVarHandle(SlidingWindowLinkedList.class, "_tail", Node.class);
			NEXT_HANDLE = l.findVarHandle(Node.class, "_next", Node.class);
			CONCURRENCY_FLAG_HANDLE = l.findVarHandle(SliderImpl.class, "_concurrencyFlag", int.class);
		} catch (ReflectiveOperationException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private final int windowSize;

	private volatile Node<E> _head;
	private volatile Node<E> _tail;
	private volatile AppendStrategy<E> appendStrategy;

	public SlidingWindowLinkedList(int windowSize) {
		if (windowSize < 0) {
			throw new IllegalArgumentException("windowSize must be >= 0");
		}
		this.windowSize = windowSize;

		Node<E> dummy = new Node<>(null);
		this._head = dummy;
		this._tail = dummy;
		this.appendStrategy = windowSize == 0 ? new WindowAppendStrategy() : new InitialAppendStrategy();
	}

	public void append(E value) {
		Objects.requireNonNull(value);
		Node<E> newNode = new Node<>(value);
		appendStrategy.append(newNode);
	}

	public Slider<E> slider() {
		return new SliderImpl<>(_head);
	}

	public List<E> getSnapshot() {
		List<E> result = new ArrayList<>(windowSize);
		int count = 0;
		var curr = _head._next;
		while (curr != null && count < windowSize) {
			result.add(curr.value);
			curr = curr._next;
		}

		return result;
	}

	public static class Node<E> {

		private final E value;
		volatile Node<E> _next;

		Node(E value) {
			this.value = value;
		}

	}

	public interface Slider<E> {

		boolean hasNext();

		E next();

	}

	private static class SliderImpl<E> implements Slider<E> {

		private volatile Node<E> cursor;
		private volatile int _concurrencyFlag;

		private SliderImpl(Node<E> head) {
			this.cursor = head;
		}

		@Override
		public boolean hasNext() {
			return cursor._next != null;
		}

		@Override
		public E next() {
			var prevFlag = (int) CONCURRENCY_FLAG_HANDLE.getAndSet(this, 1);
			if (prevFlag == 1) {
				throw new IllegalStateException("next() called concurrently by multiple threads!");
			}
			try {
				var curr = cursor;
				var next = curr._next;
				if (next != null) {
					cursor = next;
					return next.value;
				} else {
					return null;
				}
			} finally {
				CONCURRENCY_FLAG_HANDLE.set(this, 0);
			}
		}

	}

	private interface AppendStrategy<E> {

		void append(Node<E> node);

	}

	private class InitialAppendStrategy implements AppendStrategy<E> {

		private final AtomicLong seqGenerator = new AtomicLong(0);

		@Override
		public void append(Node<E> node) {
			while (true) {
				Node<E> currTail = _tail;

				if (NEXT_HANDLE.weakCompareAndSet(currTail, null, node)) {
					if (!(appendStrategy instanceof InitialAppendStrategy)) {
						NEXT_HANDLE.setVolatile(currTail, null);
						appendStrategy.append(node);
						return;
					}

					long seqNumber = seqGenerator.incrementAndGet();
					if (seqNumber == windowSize) {
						appendStrategy = new WindowAppendStrategy();
					}

					TAIL_HANDLE.setVolatile(SlidingWindowLinkedList.this, node);
					return;
				}
			}
		}

	}

	private class WindowAppendStrategy implements AppendStrategy<E> {

		@Override
		public void append(Node<E> node) {
			while (true) {
				Node<E> currTail = _tail;

				if (NEXT_HANDLE.weakCompareAndSet(currTail, null, node)) {
					HEAD_HANDLE.setVolatile(SlidingWindowLinkedList.this, _head._next);
					TAIL_HANDLE.setVolatile(SlidingWindowLinkedList.this, node);
					return;
				}
			}
		}

	}

}
