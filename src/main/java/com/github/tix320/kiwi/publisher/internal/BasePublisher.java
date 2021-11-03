package com.github.tix320.kiwi.publisher.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import com.github.tix320.kiwi.observable.Observable;
import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.publisher.Publisher;
import com.github.tix320.kiwi.publisher.PublisherCompletedException;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BasePublisher<T> extends Publisher<T> {

	private final List<PublisherSubscription<T>> subscriptions;

	private final List<T> queue;

	private final int saveOnCleanup;

	private final int cleanupThreshold;

	private volatile boolean isFrozen;

	private volatile SourceCompletion completion;

	private volatile int cleanupCounter;

	private volatile int cleanCount;

	protected BasePublisher(int saveOnCleanup, int cleanupThreshold) {
		this.subscriptions = new CopyOnWriteArrayList<>();
		this.queue = new ArrayList<>();
		this.saveOnCleanup = saveOnCleanup;
		this.cleanupThreshold = cleanupThreshold;
		this.isFrozen = false;
		this.completion = null;
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
				int minCursor = subscriptions.stream()
						.mapToInt(PublisherSubscription::cursor)
						.map(operand -> operand - cleanCount)
						.min()
						.orElse(queueSize);

				int deleteCount = Math.min(queueSize - saveOnCleanup, minCursor);

				queue.subList(0, deleteCount).clear();

				cleanCount += deleteCount;
			}

			queue.add(object);
			if (!isFrozen) {
				subscriptions.forEach(PublisherSubscription::tryDoAction);
			}

			postPublish();
		}
	}

	@Override
	public final void complete(SourceCompletion sourceCompletion) {
		synchronized (this) {
			if (completion != null) {
				return;
			}
			completion = sourceCompletion;

			if (!isFrozen) {
				subscriptions.forEach(PublisherSubscription::tryDoAction);
				subscriptions.clear();
			}
		}
	}

	@Override
	public final boolean isCompleted() {
		return completion != null;
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
				subscriptions.forEach(PublisherSubscription::tryDoAction);
			}
		}
	}

	public final boolean isFrozen() {
		return isFrozen;
	}

	public SourceCompletion getCompletion() {
		return completion;
	}

	public int getPublishCount() {
		return queue.size() + cleanCount;
	}

	public final T getNthPublish(int n) {
		//		T value = queue.get(index).getValue();
		//		if(value.equals(4)){
		//			try {
		//				Thread.sleep(500);
		//			} catch (InterruptedException e) {
		//				e.printStackTrace();
		//			}
		//		}
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
			return queue.get(index);
		}
	}

	protected final List<T> queueSnapshot(int fromIndex, int toIndex) {
		synchronized (this) {
			return queue.subList(fromIndex, toIndex);
		}
	}

	void removeSubscription(PublisherSubscription<T> subscription) {
		synchronized (this) {
			this.subscriptions.remove(subscription);
		}
	}

	private void checkCompleted() {
		if (completion != null) {
			throw new PublisherCompletedException("Publisher is completed, you can not publish items.");
		}
	}

	private final class PublisherObservable extends Observable<T> {

		@Override
		public void subscribe(Subscriber<? super T> subscriber) {
			int initialCursor;
			synchronized (BasePublisher.this) {
				initialCursor = BasePublisher.this.resolveInitialCursorOnSubscribe() + cleanCount;
			}

			PublisherSubscription<T> subscription = new PublisherSubscription<>(BasePublisher.this, subscriber,
					initialCursor);

			subscriber.setSubscription(subscription);

			synchronized (BasePublisher.this) {
				subscriptions.add(subscription);
			}

			subscription.startWork();
		}
	}
}
