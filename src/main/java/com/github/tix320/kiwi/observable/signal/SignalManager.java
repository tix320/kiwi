package com.github.tix320.kiwi.observable.signal;

import java.util.Comparator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.github.tix320.kiwi.observable.scheduler.DefaultScheduler;
import com.github.tix320.skimp.api.exception.ExceptionUtils;

public final class SignalManager {

	private static final AtomicReferenceFieldUpdater<SignalManager, State> stateUpdater = AtomicReferenceFieldUpdater.newUpdater(
			SignalManager.class, State.class, "state");

	private static final AtomicIntegerFieldUpdater<SignalManager> workerStateUpdater = AtomicIntegerFieldUpdater.newUpdater(
			SignalManager.class, "workerState");

	private static final Comparator<Signal> DEFAULT_COMPARATOR = Comparator.comparing(Signal::defaultPriority)
			.reversed()
			.thenComparing(Signal::order);

	private static final int WORKER_NOT_RUNNING = 0;
	private static final int WORKER_WAITING = 1;
	private static final int WORKER_RUNNING = 2;

	private final PriorityBlockingQueue<QueueItem> signalQueue;

	private volatile State state;

	private volatile int workerState;

	public SignalManager(int initialSourcesCount) {
		this(initialSourcesCount, DEFAULT_COMPARATOR);
	}

	private SignalManager(int initialSourcesCount, Comparator<? super Signal> comparator) {
		if (initialSourcesCount <= 0) {
			throw new IllegalArgumentException(String.valueOf(initialSourcesCount));
		}
		signalQueue = new PriorityBlockingQueue<>(16, (o1, o2) -> comparator.compare(o1.signal, o2.signal));
		state = new State(initialSourcesCount, 0, 0);
	}

	public void increaseTokensCount(int n) {
		if (n < 0) {
			throw new IllegalArgumentException(String.valueOf(n));
		}

		if (n == 0) {
			return;
		}

		stateUpdater.updateAndGet(this, currentState -> {
			if (currentState.isDone()) {
				throw new IllegalStateException("Already started");
			}

			return new State(currentState.registrationsCount + n, currentState.tokensCreated, currentState.startCalled);
		});
	}

	public Token createToken(SignalVisitor signalVisitor) {
		stateUpdater.updateAndGet(this, currentState -> {
			if (currentState.tokensCreated == currentState.registrationsCount) {
				throw new IllegalStateException("Tokens not available");
			}

			return new State(currentState.registrationsCount, currentState.tokensCreated + 1, currentState.startCalled);
		});
		return new TokenImpl(signalVisitor);
	}

	private void start() {
		boolean changed = workerStateUpdater.compareAndSet(this, WORKER_NOT_RUNNING, WORKER_WAITING);
		if (!changed) {
			throw new IllegalStateException();
		}


		tryRunWorker();
	}

	private void tryRunWorker() {
		boolean changed = workerStateUpdater.compareAndSet(this, WORKER_WAITING, WORKER_RUNNING);
		if (changed) {
			DefaultScheduler.get().schedule(() -> {
				QueueItem queueItem;
				loop:
				while ((queueItem = signalQueue.poll()) != null) {

					Signal signal = queueItem.signal;
					Owner owner = queueItem.owner();
					if (owner.isCompleted()) {
						continue;
					}
					SignalVisitor visitor = owner.getSignalVisitor();

					try {
						SignalVisitor.SignalVisitResult visitResult = signal.accept(visitor);
						switch (visitResult) {
							case CONTINUE -> {
							}
							case REQUEUE_AND_PAUSE -> {
								signalQueue.add(queueItem);
								break loop;
							}
							case COMPLETE -> owner.complete();
						}
					}
					catch (Throwable e) {
						// TODO call OnError
						ExceptionUtils.applyToUncaughtExceptionHandler(e);
						return;
					}
				}


				if (!workerStateUpdater.compareAndSet(this, WORKER_RUNNING, WORKER_WAITING)) {
					throw new IllegalStateException();
				}

				if (queueItem == null) { // loop is break because of queue was empty
					QueueItem peek = signalQueue.peek();
					if (peek != null) { // in case another wanted to start a worker, but could not because of the flag status
						tryRunWorker();
					}
				}
			});
		}
	}

	public interface Token {

		void addSignal(Signal signal);

		void start();

		void tryRunWorker();
	}

	private static record QueueItem(Signal signal, Owner owner) {

	}

	private static final class Owner {
		private final SignalVisitor signalVisitor;
		private volatile boolean completed;

		private Owner(SignalVisitor signalVisitor) {
			this.signalVisitor = signalVisitor;
		}

		public void complete() {
			completed = true;
		}

		public boolean isCompleted() {
			return completed;
		}

		public SignalVisitor getSignalVisitor() {
			return signalVisitor;
		}
	}

	private static record State(int registrationsCount, int tokensCreated, int startCalled) {

		public boolean isDone() {
			return registrationsCount == startCalled;
		}
	}

	private final class TokenImpl implements Token {

		private volatile boolean started = false;

		private final Owner owner;

		private TokenImpl(SignalVisitor signalVisitor) {
			this.owner = new Owner(signalVisitor);
		}

		@Override
		public void addSignal(Signal signal) {
			signalQueue.add(new QueueItem(signal, owner));
		}

		@Override
		public void start() {
			synchronized (this) {
				if (started) {
					throw new IllegalStateException("Already started");
				}

				started = true;

				State state = stateUpdater.updateAndGet(SignalManager.this,
						currentState -> new State(currentState.registrationsCount, currentState.tokensCreated,
								currentState.startCalled + 1));

				if (state.isDone()) {
					SignalManager.this.start();
				}
			}

		}

		@Override
		public void tryRunWorker() {
			SignalManager.this.tryRunWorker();
		}
	}
}
