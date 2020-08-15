package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.PublisherCompletedException;
import com.github.tix320.kiwi.api.util.ExceptionUtils;
import com.github.tix320.kiwi.api.util.Threads;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BasePublisher<T> implements Publisher<T> {

	private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 2, TimeUnit.MINUTES,
			new SynchronousQueue<>(), Threads::daemon);

	private final List<InternalSubscription<T>> subscriptions;

	protected final List<T> queue;

	private volatile boolean freeze;

	protected final AtomicBoolean isCompleted;

	private final int saveOnCleanup;

	private final int cleanupThreshold;

	private volatile int cleanupCounter = 0;

	protected BasePublisher(int saveOnCleanup, int cleanupThreshold) {
		this.saveOnCleanup = saveOnCleanup;
		this.cleanupThreshold = cleanupThreshold;
		queue = new ArrayList<>();
		isCompleted = new AtomicBoolean(false);
		freeze = false;
		subscriptions = new CopyOnWriteArrayList<>();
	}

	@Override
	public final void publish(T object) {
		synchronized (this) {
			cleanupCounter++;
			if (cleanupCounter == cleanupThreshold) {
				cleanupCounter = 0;

				int queueSize = queue.size();
				int minCursor = queueSize;
				for (InternalSubscription<T> subscription : subscriptions) {
					if (subscription.cursor < minCursor) {
						minCursor = subscription.cursor;
					}
				}

				int deleteCount = Math.min(queueSize - saveOnCleanup, minCursor);

				queue.subList(0, deleteCount).clear();

				for (InternalSubscription<T> subscription : subscriptions) {
					subscription.cursor = subscription.cursor - deleteCount;
				}
			}
		}

		publishOverride(object);
	}

	protected abstract void publishOverride(T object);

	@Override
	public final void complete() {
		Iterator<InternalSubscription<T>> iterator;
		synchronized (this) {
			if (isCompleted.get()) {
				return;
			}
			isCompleted.set(true);
			iterator = subscriptions.iterator();
			subscriptions.clear();
		}

		iterator.forEachRemaining(InternalSubscription::complete);
	}

	@Override
	public final boolean isCompleted() {
		return isCompleted.get();
	}

	@Override
	public Observable<T> asObservable() {
		return new PublisherObservable();
	}

	public void freeze() {
		synchronized (this) {
			freeze = true;
		}
	}

	public void unfreeze() {
		synchronized (this) {
			freeze = false;
			for (InternalSubscription<T> subscription : subscriptions) {
				subscription.publish();
			}
		}
	}

	public boolean isFreeze() {
		return freeze;
	}

	/**
	 * This method called on same thread of subscriber, avoid concurrency side effects.
	 */
	protected abstract void subscribe(InternalSubscription<T> subscription);

	protected final void checkCompleted() {
		if (isCompleted.get()) {
			throw createCompletedException();
		}
	}

	protected final Iterator<InternalSubscription<T>> getSubscriptionsIterator() {
		return subscriptions.iterator();
	}

	private PublisherCompletedException createCompletedException() {
		return new PublisherCompletedException("Publisher is completed, you can not publish items.");
	}

	public static void runAsync(CheckedRunnable runnable) {
		StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
		EXECUTOR.submit(() -> {
			try {
				runnable.run();
			}
			catch (Throwable e) {
				ExceptionUtils.appendAsyncStacktrace(stackTrace, e);
				ExceptionUtils.applyToUncaughtExceptionHandler(e);
			}
		});
	}

	public final class PublisherObservable implements Observable<T> {

		@Override
		public void subscribe(Subscriber<? super T> subscriber) {
			InternalSubscription<T> subscription = new InternalSubscription<>(BasePublisher.this, subscriber);
			boolean needRegister = subscriber.onSubscribe(subscription);
			if (!needRegister) {
				subscriber.onComplete(CompletionType.UNSUBSCRIPTION);
				return;
			}

			synchronized (BasePublisher.this) {
				if (!isCompleted.get()) {
					subscriptions.add(subscription);
				}
				BasePublisher.this.subscribe(subscription);
			}
		}
	}

	protected static final class InternalSubscription<I> implements Subscription {

		private final BasePublisher<I> publisher;

		private final Subscriber<? super I> realSubscriber;

		private volatile int cursor;

		private volatile boolean publishInProgress;

		private volatile Completion completion;

		private InternalSubscription(BasePublisher<I> publisher, Subscriber<? super I> realSubscriber) {
			this(publisher, realSubscriber, 0);
		}

		private InternalSubscription(BasePublisher<I> publisher, Subscriber<? super I> realSubscriber,
									 int initialCursor) {
			if (initialCursor < 0) {
				throw new IllegalStateException();
			}
			this.publisher = publisher;
			this.realSubscriber = realSubscriber;
			this.cursor = initialCursor;
			this.publishInProgress = false;
			this.completion = null;
		}

		public void changeCursor(int newCursor) {
			this.cursor = newCursor;
		}

		public void publish() {
			boolean runPublish = false;
			boolean runComplete = false;
			synchronized (publisher) {
				if (!publishInProgress && !publisher.freeze) {
					if (completion != null) {
						if (cursor <= completion.cursor) {
							publishInProgress = true;
							runPublish = true;
						}
						else {
							if (!completion.onCompleteCalled) {
								completion = completion.changeOnCompleteCalled(true);
								runComplete = true;
							}
						}
					}
					else if (cursor < publisher.queue.size()) {
						publishInProgress = true;
						runPublish = true;
					}
				}
			}

			if (runComplete) {
				runAsync(() -> realSubscriber.onComplete(completion.type));
			}

			else if (runPublish) {
				runAsync(() -> {
					List<I> queue = publisher.queue;
					I item;
					synchronized (publisher) {
						item = queue.get(cursor);
					}
					boolean needMore = true;

					Throwable subscriberError = null;
					try {
						needMore = realSubscriber.onPublish(item);
					}
					catch (Throwable e) {
						subscriberError = e;
					}

					boolean removed = false;
					synchronized (publisher) {
						publishInProgress = false;

						cursor++;

						if (needMore) {
							publish();
						}
						else {
							publisher.subscriptions.remove(this);
							completion = new Completion(cursor - 1, CompletionType.UNSUBSCRIPTION, true);
							removed = true;
						}
					}
					if (removed) {
						realSubscriber.onComplete(CompletionType.UNSUBSCRIPTION);
					}

					if (subscriberError != null) {
						throw subscriberError;
					}
				});
			}

		}

		public void complete() {
			boolean needCallOnComplete = false;
			synchronized (publisher) {
				if (completion == null) {
					if (publishInProgress) {
						completion = new Completion(publisher.queue.size() - 1, CompletionType.SOURCE_COMPLETED, false);
					}
					else {
						completion = new Completion(publisher.queue.size() - 1, CompletionType.SOURCE_COMPLETED, true);
						needCallOnComplete = true;
					}
				}
			}
			if (needCallOnComplete) {
				runAsync(() -> realSubscriber.onComplete(CompletionType.SOURCE_COMPLETED));
			}
		}

		@Override
		public boolean isCompleted() {
			return completion != null;
		}

		@Override
		public void unsubscribe() {
			boolean needCallOnComplete = false;
			synchronized (publisher) {
				if (completion == null) {
					if (publishInProgress) {
						completion = new Completion(publisher.queue.size() - 1, CompletionType.UNSUBSCRIPTION, false);
					}
					else {
						publisher.subscriptions.remove(this);
						completion = new Completion(publisher.queue.size() - 1, CompletionType.UNSUBSCRIPTION, true);
						needCallOnComplete = true;
					}
				}
			}
			if (needCallOnComplete) {
				runAsync(() -> realSubscriber.onComplete(CompletionType.UNSUBSCRIPTION));
			}
		}

		private static final class Completion {

			private final int cursor;

			private final CompletionType type;

			private final boolean onCompleteCalled;

			private Completion(int cursor, CompletionType type, boolean onCompleteCalled) {
				this.cursor = cursor;
				this.type = type;
				this.onCompleteCalled = onCompleteCalled;
			}

			public int getCursor() {
				return cursor;
			}

			public CompletionType getType() {
				return type;
			}

			public boolean isOnCompleteCalled() {
				return onCompleteCalled;
			}

			public Completion changeOnCompleteCalled(boolean value) {
				return new Completion(cursor, type, value);
			}
		}

	}
}
