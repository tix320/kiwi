package com.github.tix320.kiwi.internal.reactive.publisher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import com.github.tix320.kiwi.api.reactive.observable.CompletionType;
import com.github.tix320.kiwi.api.reactive.observable.Observable;
import com.github.tix320.kiwi.api.reactive.observable.Subscriber;
import com.github.tix320.kiwi.api.reactive.observable.Subscription;
import com.github.tix320.kiwi.api.reactive.publisher.Publisher;
import com.github.tix320.kiwi.api.reactive.publisher.PublisherCompletedException;
import com.github.tix320.skimp.api.exception.ExceptionUtils;
import com.github.tix320.skimp.api.function.CheckedRunnable;
import com.github.tix320.skimp.api.thread.Threads;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BasePublisher<T> implements Publisher<T> {

	public static final boolean ENABLE_ASYNC_STACKTRACE = System.getProperty("kiwi.publisher.async-stacktrace.enable")
														  != null;

	private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 2, TimeUnit.MINUTES,
			new SynchronousQueue<>(), Threads::daemon);

	private final List<InternalSubscription<T>> subscriptions;

	private final List<Item<T>> queue;

	private final int saveOnCleanup;

	private final int cleanupThreshold;

	private volatile boolean isFrozen;

	private volatile boolean isCompleted;

	private volatile int cleanupCounter;

	protected BasePublisher(int saveOnCleanup, int cleanupThreshold) {
		this.subscriptions = new CopyOnWriteArrayList<>();
		this.queue = new ArrayList<>();
		this.saveOnCleanup = saveOnCleanup;
		this.cleanupThreshold = cleanupThreshold;
		this.isFrozen = false;
		this.isCompleted = false;
		this.cleanupCounter = 0;
	}

	@Override
	public final void publish(T object) {
		synchronized (this) {
			checkCompleted();
			cleanupCounter++;
			if (cleanupCounter == cleanupThreshold) {
				cleanupCounter = 0;

				int queueSize = queue.size();
				int minCursor = subscriptions.stream().mapToInt(InternalSubscription::cursor).min().orElse(queueSize);

				int deleteCount = Math.min(queueSize - saveOnCleanup, minCursor);

				queue.subList(0, deleteCount).clear();

				for (InternalSubscription<T> subscription : subscriptions) {
					subscription.changeCursor(subscription.cursor() - deleteCount);
				}
			}

			addToQueue(object);
			if (!isFrozen) {
				subscriptions.forEach(InternalSubscription::tryDoAction);
			}

			postPublish();
		}
	}

	@Override
	public final void complete() {
		synchronized (this) {
			if (isCompleted) {
				return;
			}
			isCompleted = true;

			subscriptions.forEach(InternalSubscription::markSourceCompleted);
			subscriptions.clear();
		}
	}

	@Override
	public final boolean isCompleted() {
		return isCompleted;
	}

	@Override
	public Observable<T> asObservable() {
		return new PublisherObservable();
	}

	public void freeze() {
		synchronized (this) {
			this.isFrozen = true;
		}
	}

	public void unfreeze() {
		synchronized (this) {
			this.isFrozen = false;
			subscriptions.forEach(InternalSubscription::tryDoAction);
		}
	}

	public boolean isFrozen() {
		return isFrozen;
	}

	protected void postPublish() {
		// No-op
	}

	protected abstract int resolveInitialCursorOnSubscribe();

	protected final int queueSize() {
		synchronized (this) {
			return queue.size();
		}
	}

	protected final T getValueAt(int index) {
		synchronized (this) {
			return queue.get(index).getValue();
		}
	}

	protected final List<T> queueSnapshot(int fromIndex, int toIndex) {
		synchronized (this) {
			return queue.subList(fromIndex, toIndex).stream().map(Item::getValue).collect(Collectors.toList());
		}
	}

	Item<T> getItemAt(int index) {
		synchronized (this) {
			return queue.get(index);
		}
	}

	void removeSubscription(InternalSubscription<T> subscription) {
		this.subscriptions.remove(subscription);
	}

	private void addToQueue(T object) {
		synchronized (this) {
			Item<T> item;

			if (ENABLE_ASYNC_STACKTRACE) {
				item = new Item<>(object, Thread.currentThread().getStackTrace());
			} else {
				item = new Item<>(object, null);
			}

			queue.add(item);
		}
	}

	private void checkCompleted() {
		if (isCompleted) {
			throw new PublisherCompletedException("Publisher is completed, you can not publish items.");
		}
	}

	public static void runAsync(CheckedRunnable runnable) {
		EXECUTOR.submit(() -> {
			try {
				runnable.run();
			} catch (Throwable e) {
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
				int initialCursor = BasePublisher.this.resolveInitialCursorOnSubscribe();
				subscription.changeCursor(initialCursor);
				subscription.tryDoAction();
				if (isCompleted) {
					subscription.markSourceCompleted();
				} else {
					subscriptions.add(subscription);
				}
			}
		}
	}
}

final class InternalSubscription<I> implements Subscription {

	private final BasePublisher<I> publisher;

	private final Subscriber<? super I> realSubscriber;

	private volatile int cursor;

	private volatile boolean actionInProgress;

	private volatile Completion completion;

	public InternalSubscription(BasePublisher<I> publisher, Subscriber<? super I> realSubscriber) {
		this(publisher, realSubscriber, 0);
	}

	private InternalSubscription(BasePublisher<I> publisher, Subscriber<? super I> realSubscriber, int initialCursor) {
		if (initialCursor < 0) {
			throw new IllegalStateException();
		}
		this.publisher = publisher;
		this.realSubscriber = realSubscriber;
		this.cursor = initialCursor;
		this.actionInProgress = false;
		this.completion = null;
	}

	public int cursor() {
		return cursor;
	}

	public void changeCursor(int newCursor) {
		this.cursor = newCursor;
	}

	public void tryDoAction() {
		synchronized (publisher) {
			if (actionInProgress || publisher.isFrozen()) {
				return;
			}

			if (completion == null) {
				if (cursor < publisher.queueSize()) {
					actionInProgress = true;
					doPublish();
				}
			} else {
				if (cursor <= completion.getLastItemIndex()) {
					actionInProgress = true;
					doPublish();
				} else {
					if (!completion.isDone()) {
						actionInProgress = true;
						doComplete();
					}
				}
			}
		}
	}

	@Override
	public boolean isCompleted() {
		Completion completion = this.completion;
		return completion != null && completion.isDone();
	}

	@Override
	public void unsubscribe() {
		StackTraceElement[] stackTrace = null;
		if (BasePublisher.ENABLE_ASYNC_STACKTRACE) {
			stackTrace = Thread.currentThread().getStackTrace();
		}

		synchronized (publisher) {
			if (this.completion == null) {
				setCompletion(new Completion(publisher.queueSize() - 1, CompletionType.UNSUBSCRIPTION, stackTrace));

				tryDoAction();
			}
		}
	}

	public void markSourceCompleted() {
		StackTraceElement[] stackTrace = null;
		if (BasePublisher.ENABLE_ASYNC_STACKTRACE) {
			stackTrace = Thread.currentThread().getStackTrace();
		}

		if (this.completion == null) {
			setCompletion(new Completion(publisher.queueSize() - 1, CompletionType.SOURCE_COMPLETED, stackTrace));

			tryDoAction();
		}
	}

	private void doPublish() {
		BasePublisher.runAsync(() -> {
			Item<I> item;
			synchronized (publisher) {
				item = publisher.getItemAt(cursor++);
			}

			boolean needMore = true;

			try {
				needMore = realSubscriber.onPublish(item.getValue());
			} catch (Throwable e) {
				if (BasePublisher.ENABLE_ASYNC_STACKTRACE) {
					ExceptionUtils.appendStacktraceToThrowable(e, item.getPublisherStackTrace());
				}
				ExceptionUtils.applyToUncaughtExceptionHandler(e);
			}

			synchronized (publisher) {
				if (!needMore) { // need force delete subscription
					publisher.removeSubscription(this);
					Completion completion = new Completion(cursor - 1, CompletionType.UNSUBSCRIPTION,
							item.getPublisherStackTrace());
					setCompletion(completion);
				}

				this.actionInProgress = false;
				tryDoAction();
			}
		});
	}

	private void doComplete() {
		BasePublisher.runAsync(() -> {
			try {
				realSubscriber.onComplete(completion.getType());
			} catch (Throwable e) {
				if (BasePublisher.ENABLE_ASYNC_STACKTRACE) {
					ExceptionUtils.appendStacktraceToThrowable(e, completion.getStacktrace());
				}
				ExceptionUtils.applyToUncaughtExceptionHandler(e);
			}

			synchronized (publisher) {
				completion.done();
				this.actionInProgress = false;
				tryDoAction();
			}
		});
	}

	private void setCompletion(Completion completion) {
		this.completion = completion;
	}
}

final class Item<I> {
	private final I value;
	private final StackTraceElement[] publisherStackTrace;

	public Item(I value, StackTraceElement[] publisherStackTrace) {
		this.value = value;
		this.publisherStackTrace = publisherStackTrace;
	}

	public I getValue() {
		return value;
	}

	public StackTraceElement[] getPublisherStackTrace() {
		return publisherStackTrace;
	}
}

final class Completion {

	private final int lastItemIndex;

	private final CompletionType type;

	private final StackTraceElement[] stacktrace;

	private volatile boolean done;

	public Completion(int lastItemIndex, CompletionType type, StackTraceElement[] stacktrace) {
		this.lastItemIndex = lastItemIndex;
		this.type = type;
		this.stacktrace = stacktrace;
		this.done = false;
	}

	public int getLastItemIndex() {
		return lastItemIndex;
	}

	public CompletionType getType() {
		return type;
	}

	public StackTraceElement[] getStacktrace() {
		return stacktrace;
	}

	public boolean isDone() {
		return done;
	}

	public void done() {
		this.done = true;
	}
}
