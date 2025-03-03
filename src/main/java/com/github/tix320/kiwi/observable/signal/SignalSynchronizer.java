package com.github.tix320.kiwi.observable.signal;

import static java.util.Comparator.comparing;
import static java.util.Comparator.nullsFirst;

import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.scheduler.KiwiSchedulerHolder;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReference;

public final class SignalSynchronizer {

	private static final AtomicIntegerFieldUpdater<SignalSynchronizer> WORKER_STATE_HANDLE =
		AtomicIntegerFieldUpdater.newUpdater(SignalSynchronizer.class, "workerState");

	private static final Comparator<Signal> CROSS_SOURCE_COMPARATOR =
		nullsFirst(comparing(Signal::seqNumber).reversed());

	private static final Comparator<Signal> SAME_SOURCE_COMPARATOR = comparing(Signal::priority)
		.thenComparing(comparing(Signal::seqNumber).reversed());

	private static final int NOT_ACTIVATED = -1;
	private static final int ACTIVATED = 0;
	private static final int ACQUIRED = 1;
	private static final int SCHEDULED = 2;

	private final Map<Token, PriorityBlockingQueue<Signal>> signalQueues;

	private final AtomicReference<State> tokensState;

	private volatile int workerState;

	public SignalSynchronizer() {
		this.signalQueues = new ConcurrentHashMap<>();
		this.tokensState = new AtomicReference<>(new State(1, 0, 0));
		this.workerState = NOT_ACTIVATED;
	}

	public void addNewTokenSpace() {
		tokensState.updateAndGet(state -> {
			if (state.tokensCapacity == state.tokensActivated) {
				throw new IllegalStateException("Tokens capacity cannot be expanded. Already fully activated");
			}

			return new State(state.tokensCapacity + 1, state.tokensCreated, state.tokensActivated);
		});
	}

	public Token createToken(Subscriber<?> subscriber) {
		tokensState.updateAndGet(currentState -> {
			if (currentState.tokensCreated == currentState.tokensCapacity) {
				throw new IllegalStateException("No free tokens available");
			}

			return new State(currentState.tokensCapacity, currentState.tokensCreated + 1, currentState.tokensActivated);
		});

		var token = new Token(subscriber);
		signalQueues.put(token, new PriorityBlockingQueue<>(16, SAME_SOURCE_COMPARATOR.reversed()));

		return token;
	}

	private void activate() {
		boolean changed = WORKER_STATE_HANDLE.compareAndSet(this, NOT_ACTIVATED, ACTIVATED);
		if (!changed) {
			throw new IllegalStateException("Already activated");
		}

		tryRunWorker();
	}

	private void tryRunWorker() {
		int newValue = WORKER_STATE_HANDLE.updateAndGet(this, value -> switch (value) {
			case NOT_ACTIVATED -> NOT_ACTIVATED;
			case ACTIVATED -> ACQUIRED;
			default -> SCHEDULED;
		});

		if (newValue == ACQUIRED) {
			KiwiSchedulerHolder.get().schedule(this::runWorker);
		}
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	private void runWorker() {
		try {
			while (true) {
				var entry = pollLeading();
				if (entry == null) {
					break;
				}

				Signal signal = entry.getValue().poll();
				if (signal == null) {
					break;
				}

				Token token = entry.getKey();

				Subscriber subscriber = token.subscriber;

				if (signal instanceof PublishSignal publishSignal) { // todo: java 21 switch
					subscriber.publish(publishSignal.getItem());
				} else if (signal instanceof CompleteSignal completeSignal) {
					subscriber.complete(completeSignal.completion());
					signalQueues.remove(token);
				} else if (signal instanceof ErrorSignal errorSignal) {
					// subscriber.completeWithError(errorSignal.error());
					signalQueues.remove(token);
				} else if (signal instanceof CancelSignal cancelSignal) {
					subscriber.complete(cancelSignal.unsubscription());
					signalQueues.remove(token);
				}

			}
		} finally {
			var prevValue = WORKER_STATE_HANDLE.getAndSet(this, ACTIVATED);
			if (prevValue == SCHEDULED) {
				tryRunWorker();
			}

		}
	}

	private Map.Entry<Token, PriorityBlockingQueue<Signal>> pollLeading() {
		var iterator = signalQueues.entrySet().iterator();
		if (!iterator.hasNext()) {
			return null;
		}

		Map.Entry<Token, PriorityBlockingQueue<Signal>> leadingQueue = iterator.next();
		Signal leading = leadingQueue.getValue().peek();
		while (iterator.hasNext()) {
			var entry = iterator.next();
			var signal = entry.getValue().peek();
			if (CROSS_SOURCE_COMPARATOR.compare(signal, leading) > 0) {
				leading = signal;
				leadingQueue = entry;
			}
		}

		return leadingQueue;
	}

	public final class Token {

		private final AtomicBoolean activated = new AtomicBoolean(false);

		private final Subscriber<?> subscriber;

		public Token(Subscriber<?> subscriber) {
			this.subscriber = subscriber;
		}

		public void activate() {
			var changed = activated.compareAndSet(false, true);
			if (!changed) {
				throw new IllegalStateException("Token is already activated");
			}

			State state = SignalSynchronizer.this.tokensState.updateAndGet(
				currentState -> new State(currentState.tokensCapacity,
										  currentState.tokensCreated,
										  currentState.tokensActivated + 1));

			if (state.tokensCapacity == state.tokensActivated) {
				SignalSynchronizer.this.activate();
			}
		}

		public void addSignal(Signal signal) {
			var queue = signalQueues.get(this);
			if (queue != null) {
				queue.add(signal);
				tryRunWorker();
			}
		}

	}

	private record State(int tokensCapacity, int tokensCreated, int tokensActivated) {

	}

}
