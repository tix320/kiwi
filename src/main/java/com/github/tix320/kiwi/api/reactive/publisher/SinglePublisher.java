package com.github.tix320.kiwi.api.reactive.publisher;

import java.util.Objects;
import java.util.concurrent.locks.LockSupport;

import com.github.tix320.kiwi.api.reactive.publisher.SinglePublisher.InnerState;
import com.github.tix320.kiwi.internal.reactive.publisher.ArrayUtils;
import com.github.tix320.kiwi.internal.reactive.publisher.BasePublisher;

public final class SinglePublisher<T> extends BasePublisher<T, InnerState> {

	public SinglePublisher() {
		State currentState = this.state.get();
		this.state.set(currentState.changeCustomState(new InnerState(null, 0, new Thread[0])));
	}

	public SinglePublisher(T initialValue) {
		State currentState = this.state.get();
		this.state.set(
				currentState.changeCustomState(new InnerState(Objects.requireNonNull(initialValue), 0, new Thread[0])));
	}

	@Override
	protected void subscribe(InternalSubscription<T> subscription) {
		State currentState = state.updateAndGet(
				state -> state.changeCustomState(state.getCustomState().incrementSubscribeProgress()));

		@SuppressWarnings("unchecked")
		T value = (T) currentState.getCustomState().getData();
		if (value != null) {
			subscription.publishItem(value);
		}

		currentState = state.updateAndGet(state -> {
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
	public void publish(T object) {
		Objects.requireNonNull(object);

		checkCompleted(this.state.get());

		state.updateAndGet(state -> state.changeCustomState(state.getCustomState().addWaitingPublisher()));

		while (true) {
			State currState = this.state.get();

			if (currState.getCustomState().getSubscribersProgress() > 0
				|| currState.getCustomState().getWaitingPublishers()[0] != Thread.currentThread()) {
				LockSupport.park();
				//noinspection ResultOfMethodCallIgnored
				Thread.interrupted();
			}
			else {
				State newState = currState.changeCustomState(currState.getCustomState().changeData(object));
				boolean changed = this.state.compareAndSet(currState, newState);
				if (changed) {
					for (InternalSubscription<T> subscription : newState.getSubscriptions()) {
						subscription.publishItem(object);
					}

					State state;
					do {
						state = this.state.get();
						newState = state.changeCustomState(state.getCustomState().removePublisher());
					} while (!this.state.compareAndSet(state, newState));
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

	public boolean CASPublish(T expected, T newValue) {
		Objects.requireNonNull(newValue);

		checkCompleted(this.state.get());

		if (this.state.get().getCustomState().getData() != expected) {
			return false;
		}

		State currState;
		state.updateAndGet(state -> state.changeCustomState(state.getCustomState().addWaitingPublisher()));

		while (true) {
			currState = this.state.get();

			if (currState.getCustomState().getData() != expected) {
				return false;
			}

			if (currState.getCustomState().getSubscribersProgress() > 0
				|| currState.getCustomState().getWaitingPublishers()[0] != Thread.currentThread()) {
				LockSupport.park();
				//noinspection ResultOfMethodCallIgnored
				Thread.interrupted();
			}
			else {
				State newState = currState.changeCustomState(currState.getCustomState().changeData(newValue));
				State previousState = this.state.compareAndExchange(currState, newState);
				if (previousState == currState) { // changed
					for (InternalSubscription<T> subscription : newState.getSubscriptions()) {
						subscription.publishItem(newValue);
					}

					State state;
					do {
						state = this.state.get();
						newState = state.changeCustomState(state.getCustomState().removePublisher());
					} while (!this.state.compareAndSet(state, newState));
					Thread[] waitingPublishers = newState.getCustomState().getWaitingPublishers();
					if (waitingPublishers.length != 0) {
						Thread firstWaiter = waitingPublishers[0];
						LockSupport.unpark(firstWaiter);
					}
					break;
				}
				else {
					if (previousState.getCustomState().getData() != expected) {
						return false;
					}
				}
			}
		}

		return true;
	}

	public T getValue() {
		@SuppressWarnings("unchecked")
		T obj = (T) state.get().getCustomState().getData();
		return obj;
	}

	static final class InnerState {
		private final Object data;
		private final int subscribersProgress;
		private final Thread[] waitingPublishers;

		private InnerState(Object data, int subscribersProgress, Thread[] waitingPublishers) {
			this.data = data;
			this.subscribersProgress = subscribersProgress;
			this.waitingPublishers = waitingPublishers;
		}

		public Object getData() {
			return data;
		}

		public int getSubscribersProgress() {
			return subscribersProgress;
		}

		public Thread[] getWaitingPublishers() {
			return waitingPublishers;
		}

		public InnerState incrementSubscribeProgress() {
			return new InnerState(data, subscribersProgress + 1, waitingPublishers);
		}

		public InnerState decrementSubscribeProgress() {
			return new InnerState(data, subscribersProgress - 1, waitingPublishers);
		}

		public InnerState addWaitingPublisher() {
			Thread[] newArray = ArrayUtils.addItem(this.waitingPublishers, Thread.currentThread(), Thread[]::new);
			return new InnerState(data, subscribersProgress, newArray);
		}

		public InnerState changeDataAndRemovePublisher(Object data) {
			Thread[] newArray = ArrayUtils.removeItem(this.waitingPublishers, Thread.currentThread(), Thread[]::new);

			return new InnerState(data, subscribersProgress, newArray);
		}

		public InnerState removePublisher() {
			Thread[] newArray = ArrayUtils.removeItem(this.waitingPublishers, Thread.currentThread(), Thread[]::new);

			return new InnerState(data, subscribersProgress, newArray);
		}

		public InnerState changeData(Object data) {
			return new InnerState(data, subscribersProgress, waitingPublishers);
		}
	}
}
