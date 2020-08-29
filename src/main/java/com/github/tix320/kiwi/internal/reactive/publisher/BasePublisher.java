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

	protected final List<Item<T>> queue;

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

	protected final void addToQueueWithStackTrace(T object) {
		Item<T> item = new Item<>(object, Thread.currentThread().getStackTrace());
		queue.add(item);
	}

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
				subscription.tryPublish();
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
		EXECUTOR.submit(() -> {
			try {
				runnable.run();
			}
			catch (Throwable e) {
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

		public void tryPublish() {
			synchronized (publisher) {
				if (publishInProgress || publisher.freeze) {
					return;
				}

				if (completion == null) {
					if (cursor < publisher.queue.size()) {
						publishInProgress = true;
					}
					else {
						return;
					}
				}
				else {
					if (cursor <= completion.cursor) {
						publishInProgress = true;
					}
					else {
						return;
					}
				}
			}

			runAsync(() -> {
				List<Item<I>> queue = publisher.queue;
				Item<I> item;
				synchronized (publisher) {
					item = queue.get(cursor++);
				}

				boolean needMore = true;

				try {
					needMore = realSubscriber.onPublish(item.value);
				}
				catch (Throwable e) {
					ExceptionUtils.appendAsyncStacktrace(item.publisherStackTrace, e);
					ExceptionUtils.applyToUncaughtExceptionHandler(e);
				}

				Completion completion = null;
				synchronized (publisher) {

					if (!needMore) { // need force delete subscription
						publisher.subscriptions.remove(this);
						completion = new Completion(cursor - 1, CompletionType.UNSUBSCRIPTION,
								item.publisherStackTrace);
					}
					else if (this.completion != null) { // SOURCE completed or unsubscribed via method unsubscribe()
						if (cursor > this.completion.cursor) {
							completion = this.completion;
						}
					}

					publishInProgress = false;
				}
				if (completion != null) {
					try {
						realSubscriber.onComplete(completion.type);
					}
					catch (Throwable e) {
						ExceptionUtils.appendAsyncStacktrace(completion.stacktrace, e);
						ExceptionUtils.applyToUncaughtExceptionHandler(e);
					}
				}
				else {
					tryPublish();
				}
			});
		}

		public void complete() {
			completeSubscription(CompletionType.SOURCE_COMPLETED);
		}

		@Override
		public boolean isCompleted() {
			return completion != null;
		}

		@Override
		public void unsubscribe() {
			completeSubscription(CompletionType.UNSUBSCRIPTION);
		}

		private void completeSubscription(CompletionType completionType) {
			boolean needCallOnComplete = false;
			synchronized (publisher) {
				if (completion != null) {
					return;
				}
				else {
					StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
					if (publishInProgress || publisher.freeze) {
						completion = new Completion(publisher.queue.size() - 1, completionType, stackTrace);
					}
					else {
						publisher.subscriptions.remove(this);
						completion = new Completion(publisher.queue.size() - 1, completionType, stackTrace);
						needCallOnComplete = true;
					}
				}
			}
			if (needCallOnComplete) {
				runAsync(() -> {
					try {
						realSubscriber.onComplete(completionType);
					}
					catch (Throwable e) {
						ExceptionUtils.appendAsyncStacktrace(completion.stacktrace, e);
						ExceptionUtils.applyToUncaughtExceptionHandler(e);
					}
				});
			}
		}

		private static final class Completion {

			private final int cursor;

			private final CompletionType type;

			private final StackTraceElement[] stacktrace;

			private Completion(int cursor, CompletionType type, StackTraceElement[] stacktrace) {
				this.cursor = cursor;
				this.type = type;
				this.stacktrace = stacktrace;
			}

			// public Completion changeOnCompleteCalled(boolean value) {
			// 	return new Completion(cursor, type, value, stacktrace);
			// }
		}
	}

	protected static final class Item<I> {
		private final I value;
		private final StackTraceElement[] publisherStackTrace;

		private Item(I value, StackTraceElement[] publisherStackTrace) {
			this.value = value;
			this.publisherStackTrace = publisherStackTrace;
		}

		public I getValue() {
			return value;
		}
	}
}
