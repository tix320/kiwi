package com.github.tix320.kiwi.publisher;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import com.github.tix320.kiwi.observable.SourceCompletion;
import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.signal.CompleteSignal;
import com.github.tix320.kiwi.observable.signal.PublishSignal;
import com.github.tix320.kiwi.observable.signal.Signal;
import com.github.tix320.kiwi.publisher.internal.FlowPublisher;
import com.github.tix320.kiwi.publisher.internal.PublisherCursor;
import com.github.tix320.kiwi.publisher.internal.PublisherSubscription;

/**
 * Buffered publisher for publishing objects.
 * Any publishing for this publisher will be buffered according to configured size,
 * and subscribers may subscribe and receive objects even after publishing, which will be in buffer in that time.
 *
 * @param <T> type of objects.
 * @author Tigran Sargsyan on 21-Feb-19
 */
public final class ReplayPublisher<T> extends FlowPublisher<T> {

	private final int bufferCapacity;

	//private final ConcurrentLinkedDeque<NextSignal<T>> buffer;

	public ReplayPublisher(int bufferCapacity) {
		this.bufferCapacity = bufferCapacity;
		//this.buffer = new ConcurrentLinkedDeque<>();
	}

	public List<T> getBuffer() {
		synchronized (lock) {
			int startCursor = Math.max(0, queue.size() - bufferCapacity);
			return queue.subList(startCursor, queue.size()).stream().map(PublishSignal::getItem).toList();
			// buffer.stream().map(NextSignal::getItem).toList();
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

	@Override
	protected int initialCursor() {
		return Math.max(0, queue.size() - bufferCapacity);
	}

	private final class NormalStrategyImpl extends NormalStrategy {
		@Override
		public void subscribe(Subscriber<? super T> subscriber, PublisherSubscription<T> subscription) {
			synchronized (lock) {
				//int startCursor = Math.max(0, queue.size() - bufferCapacity);
				//for (int i = startCursor; i < queue.size(); i++) {
				//	NextSignal<T> nextSignal = queue.get(i);
				//	subscription.enqueue(nextSignal);
				//}

				//if (isCompleted()) {
				//	subscription.enqueue(completion);
				//}
				//else {
					subscriptions.add(subscription);
				//}
			}

			subscriber.setSubscription(subscription);

			subscription.start();
		}

		@Override
		public void publish(T item) {
			PublishSignal<T> publishSignal = new PublishSignal<>(item);
			queue.add(publishSignal);

			for (PublisherSubscription<T> subscription : subscriptions) {
				subscription.doAction();
			}
		}

		@Override
		public void complete(SourceCompletion sourceCompletion) {
			completion = new CompleteSignal(sourceCompletion);
			subscriptions.forEach(PublisherSubscription::doAction);
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
				subscriptions.forEach(PublisherSubscription::doAction);
				subscriptions.clear();
			}
		}
	}
}
