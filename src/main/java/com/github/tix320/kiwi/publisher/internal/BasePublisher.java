package com.github.tix320.kiwi.publisher.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.publisher.PublisherCompletedException;
import com.github.tix320.skimp.api.exception.ExceptionUtils;
import com.github.tix320.skimp.api.function.CheckedRunnable;
import com.github.tix320.skimp.api.thread.Threads;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BasePublisher<T> implements Publisher<T> {

	// public static final boolean ENABLE_TRACER = System.getProperty("kiwi.publisher.tracer.enable")
	// 											!= null;

	private static final ExecutorService EXECUTOR = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 2, TimeUnit.MINUTES,
			new LinkedBlockingQueue<>(), Threads::daemon);

	private final List<SimpleSubscription<T>> subscriptions;

	private final List<PublishSignal<T>> queue;

	private final int saveOnCleanup;

	private final int cleanupThreshold;

	private volatile boolean isFrozen;

	private volatile CompleteSignal completeSignal;

	private volatile int cleanupCounter;

	private volatile int cleanCount;

	protected BasePublisher(int saveOnCleanup, int cleanupThreshold) {
		this.subscriptions = new CopyOnWriteArrayList<>();
		this.queue = new ArrayList<>();
		this.saveOnCleanup = saveOnCleanup;
		this.cleanupThreshold = cleanupThreshold;
		this.isFrozen = false;
		this.completeSignal = null;
		this.cleanupCounter = 0;
		this.cleanCount = 0;
	}

	@Override
	public final void publish(T object) {
		synchronized (this) {
			checkCompleted();
			cleanupCounter++;
			if (cleanupCounter == cleanupThreshold) {
				cleanupCounter = 0;

				int queueSize = queue.size();
				int minCursor = subscriptions.stream().mapToInt(SimpleSubscription::cursor).min().orElse(queueSize); // TODO es cursor urish bana het tali arden

				int deleteCount = Math.min(queueSize - saveOnCleanup, minCursor);

				queue.subList(0, deleteCount).clear();

				cleanCount += deleteCount;
			}

			queue.add(new PublishSignal<>(object));
			if (!isFrozen) {
				subscriptions.forEach(SimpleSubscription::tryDoAction);
			}

			postPublish();
		}
	}

	@Override
	public final void complete() {
		synchronized (this) {
			if (completeSignal != null) {
				return;
			}
			completeSignal = new CompleteSignal();

			if (!isFrozen) {
				subscriptions.forEach(SimpleSubscription::tryDoAction);
				subscriptions.clear();
			}
		}
	}

	@Override
	public final boolean isCompleted() {
		return completeSignal != null;
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
			if (this.isFrozen) {
				this.isFrozen = false;
				subscriptions.forEach(SimpleSubscription::tryDoAction);
			}
		}
	}

	public final boolean isFrozen() {
		return isFrozen;
	}

	public final PublishSignal<T> getNthPublish(int n) {
		return queue.get(n - cleanCount);
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
			return queue.get(index).value();
		}
	}

	protected final List<T> queueSnapshot(int fromIndex, int toIndex) {
		synchronized (this) {
			return queue.subList(fromIndex, toIndex).stream().map(PublishSignal::value).collect(Collectors.toList());
		}
	}

	void removeSubscription(SimpleSubscription<T> subscription) {
		synchronized (this) {
			this.subscriptions.remove(subscription);
		}
	}

	private void checkCompleted() {
		if (completeSignal != null) {
			throw new PublisherCompletedException("Publisher is completed, you can not publish items.");
		}
	}

	// public static <T> Observable<T> merge(BasePublisher<T>... publishers) {
	// 	for (BasePublisher<T> publisher : publishers) {
	// 		publisher.asObservable().subscribe();
	// 	}
	// }

	public static CompletableFuture<Void> runAsync(CheckedRunnable runnable) {
		return CompletableFuture.runAsync(() -> {
			try {
				runnable.run();
			} catch (Throwable e) {
				ExceptionUtils.applyToUncaughtExceptionHandler(e);
			}
		}, EXECUTOR);
	}

	public final class PublisherObservable implements Observable<T> {

		@Override
		public void subscribe(Subscriber<? super T> subscriber) {
			SimpleSubscription<T> subscription = new SimpleSubscription<>(BasePublisher.this, subscriber);
			subscriber.onSubscribe(subscription);

			synchronized (BasePublisher.this) {
				int initialCursor = BasePublisher.this.resolveInitialCursorOnSubscribe();
				subscription.changeCursor(initialCursor);
				subscriptions.add(subscription);
				subscription.tryDoAction();
			}
		}

		@Override
		public void subscribe(SharedSubscriber<? super T> subscriber) {

		}
	}
}
