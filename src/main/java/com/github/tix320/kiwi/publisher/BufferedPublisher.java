package com.github.tix320.kiwi.publisher;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.signal.CompleteSignal;
import com.github.tix320.kiwi.observable.signal.NextSignal;
import com.github.tix320.kiwi.publisher.internal.BasePublisher;
import com.github.tix320.kiwi.publisher.internal.PublisherSubscription;

/**
 * Buffered publisher for publishing objects.
 * Any publishing for this publisher will be buffered according to configured size,
 * and subscribers may subscribe and receive objects even after publishing, which will be in buffer in that time.
 *
 * @param <T> type of objects.
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class BufferedPublisher<T> extends BasePublisher<T> {

	private final int bufferCapacity;

	private final ConcurrentLinkedDeque<NextSignal<T>> buffer;

	public BufferedPublisher(int bufferCapacity) {
		this.bufferCapacity = bufferCapacity;
		this.buffer = new ConcurrentLinkedDeque<>();
	}

	public List<T> getBuffer() {
		synchronized (lock) {
			return buffer.stream().map(NextSignal::getItem).toList();
		}
	}

	@Override
	protected NormalStrategy getNormalStrategy() {
		return new NormalStrategyImpl();
	}

	@Override
	protected FreezeStrategy getFreezeStrategy() {
		return new FreezeStrategyImpl();
	}

	private final class NormalStrategyImpl extends NormalStrategy {
		@Override
		public void subscribe(Subscriber<? super T> subscriber, PublisherSubscription<T> subscription) {
			synchronized (lock) {
				for (NextSignal<T> nextSignal : buffer) {
					subscription.enqueue(nextSignal);
				}
				if (isCompleted()) {
					subscription.enqueue(completion);
				}
				else {
					subscriptions.add(subscription);
				}
			}

			subscriber.setSubscription(subscription);

			subscription.start();
		}

		@Override
		public void publish(T item) {
			NextSignal<T> nextSignal = new NextSignal<>(item);
			if (buffer.size() == bufferCapacity) {
				buffer.removeFirst();
			}
			buffer.addLast(nextSignal);

			for (PublisherSubscription<T> subscription : subscriptions) {
				subscription.next(nextSignal);
			}
		}

		@Override
		public void complete(SourceCompletion sourceCompletion) {
			completion = new CompleteSignal(sourceCompletion);
			subscriptions.forEach(subscription -> subscription.complete(completion));
			subscriptions.clear();
		}
	}

	private final class FreezeStrategyImpl extends FreezeStrategy {

		private final NormalStrategy normalStrategy = new NormalStrategyImpl();

		private final ConcurrentLinkedDeque<T> bufferDuringFreeze = new ConcurrentLinkedDeque<>();

		@Override
		public void subscribe(Subscriber<? super T> subscriber, PublisherSubscription<T> subscription) {
			synchronized (lock) {
				subscriptions.add(subscription);
			}

			subscriber.setSubscription(subscription);

			subscription.start();
		}

		@Override
		public void publish(T item) {
			if (bufferDuringFreeze.size() == bufferCapacity) {
				bufferDuringFreeze.removeFirst();
			}
			bufferDuringFreeze.addLast(item);
		}

		@Override
		public void complete(SourceCompletion sourceCompletion) {
			completionDuringFreeze = sourceCompletion;
		}

		@Override
		protected void restore() {
			for (T item : bufferDuringFreeze) {
				normalStrategy.publish(item);
			}
			bufferDuringFreeze.clear();

			if (completionDuringFreeze != null) {
				completion = new CompleteSignal(completionDuringFreeze);
				subscriptions.forEach(subscription -> subscription.complete(completion));
				subscriptions.clear();
			}
		}
	}
}
