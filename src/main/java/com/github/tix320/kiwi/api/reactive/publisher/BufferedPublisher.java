package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.LockSupport;

import com.github.tix320.kiwi.api.reactive.publisher.BufferedPublisher.InnerState;
import com.github.tix320.kiwi.internal.reactive.publisher.ArrayUtils;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

/**
 * Buffered publisher for publishing objects.
 * Any publishing for this publisher will be buffered according to configured size,
 * and subscribers may subscribe and receive objects even after publishing, which will be in buffer in that time.
 *
 * @param <T> type of objects.
 *
 * @author Tigran Sargsyan on 21-Feb-19
 */
public class BufferedPublisher<T> extends BasePublisher<T, InnerState> {

	private final int bufferCapacity;

	protected final ConcurrentLinkedDeque<T> buffer;

	public BufferedPublisher(int bufferCapacity) {
		this.bufferCapacity = bufferCapacity;
		this.state.updateAndGet(state -> state.changeCustomState(new InnerState(0, new Thread[0])));
		this.buffer = new ConcurrentLinkedDeque<>();
	}

	@Override
	protected final void subscribe(InternalSubscription<T> subscription) {
		synchronized (this) {
			state.updateAndGet(state -> state.changeCustomState(state.getCustomState().incrementSubscribeProgress()));
			@SuppressWarnings("unchecked")
			T[] items = (T[]) buffer.toArray();
			subscription.publishItems(items); // async
		}

		State currentState = state.updateAndGet(state -> {
			State newState = state.changeCustomState(state.getCustomState().decrementSubscribeProgress());
			if (state.isCompleted()) {
				subscription.complete();
			}
			else {
				newState = newState.addSubscription(subscription);
			}

			return newState;
		});

		if (currentState.getCustomState().getSubscribersProgress() == 0) {
			Thread[] waitingPublishers = currentState.getCustomState().getWaitingPublishers();
			if (waitingPublishers.length != 0) {
				Thread firstWaiter = waitingPublishers[0];
				LockSupport.unpark(firstWaiter);
			}
		}
	}

	@Override
	public final void publish(T object) {
		Objects.requireNonNull(object);

		checkCompleted(this.state.get());

		State currState;
		state.updateAndGet(state -> state.changeCustomState(state.getCustomState().addWaitingPublisher()));

		while (true) {
			currState = this.state.get();

			if (currState.getCustomState().getSubscribersProgress() > 0
				|| currState.getCustomState().getWaitingPublishers()[0] != Thread.currentThread()) {
				LockSupport.park();
				//noinspection ResultOfMethodCallIgnored
				Thread.interrupted();
			}
			else {
				synchronized (this) {
					currState = this.state.get();
					if (currState.getCustomState().getSubscribersProgress() == 0) {
						State newState = this.state.updateAndGet(
								state -> state.changeCustomState(state.getCustomState().removePublisher()));
						addToBuffer(object);
						for (InternalSubscription<T> subscription : currState.getSubscriptions()) {
							subscription.publishItem(object);
						}

						Thread[] waitingPublishers = newState.getCustomState().getWaitingPublishers();
						if (waitingPublishers.length != 0) {
							Thread firstWaiter = waitingPublishers[0];
							LockSupport.unpark(firstWaiter);
						}
						break;
					}
				}
			}
		}
	}

	public final List<T> getBuffer() {
		return List.copyOf(buffer);
	}

	protected void addToBuffer(T item) {
		if (buffer.size() == bufferCapacity) {
			buffer.removeFirst();
		}
		buffer.addLast(item);
	}

	static final class InnerState {
		private final int subscribersProgress;
		private final Thread[] waitingPublishers;

		private InnerState(int subscribersProgress, Thread[] waitingPublishers) {
			this.subscribersProgress = subscribersProgress;
			this.waitingPublishers = waitingPublishers;
		}

		public int getSubscribersProgress() {
			return subscribersProgress;
		}

		public Thread[] getWaitingPublishers() {
			return waitingPublishers;
		}

		public InnerState incrementSubscribeProgress() {
			return new InnerState(subscribersProgress + 1, waitingPublishers);
		}

		public InnerState decrementSubscribeProgress() {
			return new InnerState(subscribersProgress - 1, waitingPublishers);
		}

		public InnerState addWaitingPublisher() {
			Thread[] newArray = ArrayUtils.addItem(this.waitingPublishers, Thread.currentThread(), Thread[]::new);
			return new InnerState(subscribersProgress, newArray);
		}

		public InnerState removePublisher() {
			Thread[] newArray = ArrayUtils.removeItem(this.waitingPublishers, Thread.currentThread(), Thread[]::new);

			return new InnerState(subscribersProgress, newArray);
		}
	}
}
