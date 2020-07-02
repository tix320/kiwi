package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.PublisherCompletedException;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BasePublisher<T, S> implements Publisher<T> {

	private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 2, TimeUnit.MINUTES,
			new SynchronousQueue<>());

	protected final AtomicReference<State> state;

	@SuppressWarnings("unchecked")
	protected BasePublisher() {
		this.state = new AtomicReference<>(new State(null, false, new InternalSubscription[0]));
	}

	@Override
	public final void complete() {
		State currentState;
		do {
			currentState = this.state.get();
			if (currentState.isCompleted()) {
				return;
			}
		} while (!this.state.compareAndSet(currentState, currentState.complete()));

		for (InternalSubscription<T> subscription : currentState.getSubscriptions()) {
			subscription.complete();
		}
	}

	@Override
	public final boolean isCompleted() {
		return state.get().isCompleted();
	}

	@Override
	public Observable<T> asObservable() {
		return new PublisherObservable();
	}

	/**
	 * This method called on same thread of subscriber, avoid concurrency side effects.
	 */
	protected abstract void subscribe(InternalSubscription<T> subscription);

	protected final void checkCompleted(State state) {
		if (state.isCompleted()) {
			throw createCompletedException();
		}
	}

	private PublisherCompletedException createCompletedException() {
		return new PublisherCompletedException("Publisher is completed, you can not publish items.");
	}

	protected static void runAsync(Runnable runnable) {
		EXECUTOR.submit(() -> {
			try {
				runnable.run();
			}
			catch (Throwable e) {
				e.printStackTrace();
			}
		});
	}

	private final class PublisherObservable implements Observable<T> {

		@Override
		public void subscribe(Subscriber<? super T> subscriber) {
			InternalSubscription<T> subscription = new InternalSubscription<>(subscriber);
			boolean needRegister = subscriber.onSubscribe(subscription);
			if (!needRegister) {
				subscriber.onComplete(CompletionType.UNSUBSCRIPTION);
				return;
			}

			BasePublisher.this.subscribe(subscription);
		}
	}

	protected final class InternalSubscription<I> implements Subscription {

		private final Subscriber<? super I> realSubscriber;

		private final AtomicReference<SubscriptionState> state;

		private InternalSubscription(Subscriber<? super I> realSubscriber) {
			this.realSubscriber = realSubscriber;
			SubscriptionState initialState = new SubscriptionState(false, false, new Thread[0], null, null);
			this.state = new AtomicReference<>(initialState);
		}

		@SuppressWarnings("unchecked")
		public void publishItem(I item) {
			publishItems((I[]) new Object[]{item});
		}

		public void publishItems(I[] items) {
			Thread callerThread = Thread.currentThread();
			AtomicBoolean callerWait = new AtomicBoolean(true);

			Runnable wakeupCaller = () -> {
				callerWait.set(false);
				LockSupport.unpark(callerThread);
			};

			runAsync(() -> {
				SubscriptionState currentSubscriptionState;

				boolean needWait = false;

				do {
					currentSubscriptionState = state.get();

					if (currentSubscriptionState.isOnCompleteCalled()) {
						wakeupCaller.run();
						remove();
						return;
					}

					if (currentSubscriptionState.isPublishLocked()
						|| currentSubscriptionState.getWaitingPublishers().length != 0) {
						needWait = true;
						break;
					}

				} while (!state.compareAndSet(currentSubscriptionState, currentSubscriptionState.lockPublish()));

				if (needWait) {
					SubscriptionState crrState;
					SubscriptionState wtf;
					do {
						crrState = state.get();
						if (crrState.isOnCompleteCalled()) {
							remove();
							wakeupCaller.run();
							return;
						}
						wtf = crrState.addWaitingPublisher();
					} while (!state.compareAndSet(crrState, wtf));

					wakeupCaller.run();

					while (true) {
						currentSubscriptionState = state.get();

						if (currentSubscriptionState.isDropWaiter()) {
							return;
						}

						if (currentSubscriptionState.isPublishLocked()
							|| currentSubscriptionState.getWaitingPublishers()[0] != Thread.currentThread()) {
							LockSupport.park();
							//noinspection ResultOfMethodCallIgnored
							Thread.interrupted();
							continue;
						}

						boolean changed = state.compareAndSet(currentSubscriptionState,
								currentSubscriptionState.lockPublishAndRemoveFromWaiters());
						if (changed) {
							break;
						}
					}
				}
				else {
					wakeupCaller.run();
				}

				boolean needMore = true;
				for (I item : items) {
					try {
						needMore = realSubscriber.onPublish(item);
					}
					catch (Throwable e) {
						ExceptionUtils.applyToUncaughtExceptionHandler(e);
					}

					if (!needMore) {
						break;
					}
				}

				if (needMore) {
					SubscriptionState subscriptionState = state.updateAndGet(SubscriptionState::unlockPublish);

					if (subscriptionState.getWaitingPublishers().length != 0) {
						Thread firstWaiter = subscriptionState.getWaitingPublishers()[0];
						LockSupport.unpark(firstWaiter);
					}
					else if (subscriptionState.isOnCompleteCalled()) {
						LockSupport.unpark(subscriptionState.getCompleter());
					}
				}
				else {
					remove();

					SubscriptionState subscriptionState;

					Thread currentThread = Thread.currentThread();
					do {
						subscriptionState = state.get();

					} while (!state.compareAndSet(subscriptionState,
							new SubscriptionState(true, true, new Thread[0], null, currentThread)));

					for (Thread waitingPublisher : subscriptionState.getWaitingPublishers()) {
						LockSupport.unpark(waitingPublisher);
					}

					if (!subscriptionState.isOnCompleteCalled()) {
						try {
							realSubscriber.onComplete(CompletionType.UNSUBSCRIPTION);
						}
						catch (Throwable t) {
							ExceptionUtils.applyToUncaughtExceptionHandler(t);
						}
					}
				}
			});

			while (callerWait.get()) {
				LockSupport.park();
				//noinspection ResultOfMethodCallIgnored
				Thread.interrupted();
			}
		}

		public void complete() {
			Thread callerThread = Thread.currentThread();
			AtomicBoolean callerWait = new AtomicBoolean(true);

			Runnable wakeupCaller = () -> {
				callerWait.set(false);
				LockSupport.unpark(callerThread);
			};

			runAsync(() -> {
				SubscriptionState currentState;
				Thread currentThread = Thread.currentThread();
				do {
					currentState = state.get();

					if (currentState.isOnCompleteCalled()) {
						wakeupCaller.run();
						return;
					}
				} while (!state.compareAndSet(currentState,
						new SubscriptionState(true, false, currentState.getWaitingPublishers(),
								currentState.getCurrentPublisher(), currentThread)));

				wakeupCaller.run();

				while (true) {
					currentState = state.get();
					if (currentState.isPublishLocked() || currentState.getWaitingPublishers().length != 0) {
						LockSupport.park();
						//noinspection ResultOfMethodCallIgnored
						Thread.interrupted();
					}
					else {
						break;
					}
				}

				try {
					realSubscriber.onComplete(CompletionType.SOURCE_COMPLETED);
				}
				catch (Throwable t) {
					ExceptionUtils.applyToUncaughtExceptionHandler(t);
				}
			});

			while (callerWait.get()) {
				LockSupport.park();
				//noinspection ResultOfMethodCallIgnored
				Thread.interrupted();
			}
		}

		@Override
		public boolean isCompleted() {
			return state.get().isOnCompleteCalled();
		}

		@Override
		public void unsubscribe() {
			boolean removed = remove();
			if (!removed) {
				return;
			}

			Thread callerThread = Thread.currentThread();
			AtomicBoolean callerWait = new AtomicBoolean(true);

			Runnable wakeupCaller = () -> {
				callerWait.set(false);
				LockSupport.unpark(callerThread);
			};

			runAsync(() -> {
				SubscriptionState currentState;

				Thread currentThread = Thread.currentThread();
				do {
					currentState = state.get();

					if (currentState.isOnCompleteCalled()) {
						wakeupCaller.run();
						return;
					}
				} while (!state.compareAndSet(currentState,
						new SubscriptionState(true, false, currentState.getWaitingPublishers(),
								currentState.getCurrentPublisher(), currentThread)));

				wakeupCaller.run();

				SubscriptionState currState;
				while (true) {
					currState = state.get();
					if (currState.isPublishLocked() || currState.getWaitingPublishers().length != 0) {
						LockSupport.park();
						//noinspection ResultOfMethodCallIgnored
						Thread.interrupted();
					}
					else {
						break;
					}
				}

				try {
					realSubscriber.onComplete(CompletionType.UNSUBSCRIPTION);
				}
				catch (Throwable t) {
					ExceptionUtils.applyToUncaughtExceptionHandler(t);
				}
			});

			while (callerWait.get()) {
				LockSupport.park();
				//noinspection ResultOfMethodCallIgnored
				Thread.interrupted();
			}
		}

		public boolean remove() {
			State currentState;
			State newState;
			do {
				currentState = BasePublisher.this.state.get();
				if (currentState.isCompleted()) {
					return false;
				}

				newState = currentState.removeSubscription(this);

				if (newState == currentState) {
					return false;
				}

			} while (!BasePublisher.this.state.compareAndSet(currentState, newState));

			return true;
		}

		public final class SubscriptionState {
			private final boolean onCompleteCalled;
			private final boolean dropWaiter;
			private final Thread[] waitingPublishers;
			private final Thread currentPublisher;
			private final Thread completer;

			private SubscriptionState(boolean onCompleteCalled, boolean dropWaiter, Thread[] waitingPublishers,
									  Thread currentPublisher, Thread completer) {
				this.onCompleteCalled = onCompleteCalled;
				this.dropWaiter = dropWaiter;
				this.waitingPublishers = waitingPublishers;
				this.currentPublisher = currentPublisher;
				this.completer = completer;
			}

			public boolean isOnCompleteCalled() {
				return onCompleteCalled;
			}

			public boolean isDropWaiter() {
				return dropWaiter;
			}

			public Thread[] getWaitingPublishers() {
				return waitingPublishers;
			}

			public Thread getCompleter() {
				return completer;
			}

			public Thread getCurrentPublisher() {
				return currentPublisher;
			}

			public SubscriptionState addWaitingPublisher() {
				Thread[] newArray = ArrayUtils.addItem(this.waitingPublishers, Thread.currentThread(), Thread[]::new);
				return new SubscriptionState(this.onCompleteCalled, dropWaiter, newArray, currentPublisher,
						this.completer);
			}

			public SubscriptionState lockPublish() {
				return new SubscriptionState(this.onCompleteCalled, dropWaiter, this.waitingPublishers,
						Thread.currentThread(), this.completer);
			}

			public SubscriptionState lockPublishAndRemoveFromWaiters() {
				Thread[] waitingPublishers = this.waitingPublishers;
				Thread currentThread = Thread.currentThread();
				Thread firstWaiter = waitingPublishers[0];

				if (currentThread != firstWaiter) {
					throw new IllegalStateException();
				}

				Thread[] newArray = ArrayUtils.removeIndex(waitingPublishers, 0, Thread[]::new);

				return new SubscriptionState(onCompleteCalled, dropWaiter, newArray, currentThread, completer);
			}

			public SubscriptionState unlockPublish() {
				return new SubscriptionState(this.onCompleteCalled, dropWaiter, this.waitingPublishers, null,
						this.completer);
			}

			public boolean isPublishLocked() {
				return currentPublisher != null;
			}

			@Override
			public String toString() {
				return "SubscriptionState{"
					   + ", onCompleteCalled="
					   + onCompleteCalled
					   + ", publishers="
					   + Arrays.toString(waitingPublishers)
					   + '}';
			}
		}
	}

	protected final class State {

		private final S customState;

		private final boolean completed;

		private final InternalSubscription<T>[] subscriptions;

		public State(S customState, boolean completed, InternalSubscription<T>[] subscriptions) {
			this.customState = customState;
			this.completed = completed;
			this.subscriptions = subscriptions;
		}

		public State changeCustomState(S obj) {
			return new State(obj, this.completed, subscriptions);
		}

		@SuppressWarnings("unchecked")
		public State complete() {
			return new State(this.customState, true, new InternalSubscription[0]);
		}

		public State addSubscription(InternalSubscription<?> subscription) {
			if (completed) {
				throw new IllegalStateException();
			}
			InternalSubscription<T>[] subscriptions = this.subscriptions;
			@SuppressWarnings("unchecked")
			InternalSubscription<T>[] newArray = (InternalSubscription<T>[]) ArrayUtils.addItem(subscriptions,
					subscription, InternalSubscription[]::new);
			return new State(this.customState, this.completed, newArray);
		}

		public State removeSubscription(InternalSubscription<?> subscription) {
			InternalSubscription<T>[] subscriptions = this.subscriptions;
			@SuppressWarnings("unchecked")
			InternalSubscription<T>[] newArray = (InternalSubscription<T>[]) ArrayUtils.removeItem(subscriptions,
					subscription, InternalSubscription[]::new);

			if (subscriptions == newArray) {
				return this;
			}
			else {
				return new State(this.customState, this.completed, newArray);
			}
		}

		public S getCustomState() {
			return customState;
		}

		public InternalSubscription<T>[] getSubscriptions() {
			return subscriptions;
		}

		public boolean isCompleted() {
			return completed;
		}
	}
}
