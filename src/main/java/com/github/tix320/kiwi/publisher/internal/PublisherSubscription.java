package com.github.tix320.kiwi.publisher.internal;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import com.github.tix320.kiwi.observable.demand.DemandStrategy;
import com.github.tix320.kiwi.observable.demand.EmptyDemandStrategy;
import com.github.tix320.kiwi.observable.signal.*;

public final class PublisherSubscription<T> implements Subscription {

	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<PublisherSubscription, DemandStrategy> demandStrategyUpdater = AtomicReferenceFieldUpdater.newUpdater(
			PublisherSubscription.class, DemandStrategy.class, "demandStrategy");

	private final BasePublisher<T> publisher;

	private final Subscriber<? super T> realSubscriber;

	private volatile DemandStrategy demandStrategy = EmptyDemandStrategy.INSTANCE;

	private final SignalManager.Token token;

	public PublisherSubscription(BasePublisher<T> publisher, Subscriber<? super T> realSubscriber) {
		this.publisher = publisher;
		this.realSubscriber = realSubscriber;

		this.token = realSubscriber.getSignalManager().createToken(new PublisherSignalVisitor());
	}

	public void start() {
		token.start();
	}

	public void enqueue(NextSignal<T> nextSignal) {
		token.addSignal(nextSignal);
	}

	public void enqueue(CompleteSignal completeSignal) {
		token.addSignal(completeSignal);
	}

	public void next(NextSignal<T> nextSignal) {
		token.addSignal(nextSignal);

		token.tryRunWorker();
	}

	public void complete(CompleteSignal cancelSignal) {
		token.addSignal(cancelSignal);

		token.tryRunWorker();
	}

	@Override
	public void request(long n) {
		if (n <= 0) {
			throw new IllegalArgumentException(String.valueOf(n));
		}

		RequestSignal requestSignal = new RequestSignal(n);

		token.addSignal(requestSignal);

		token.tryRunWorker();
	}

	@Override
	public void cancel(Unsubscription unsubscription) {
		CancelSignal cancelSignal = new CancelSignal(unsubscription);

		token.addSignal(cancelSignal);

		token.tryRunWorker();
	}

	private final class PublisherSignalVisitor implements SignalVisitor {

		@Override
		public SignalVisitResult visit(RequestSignal requestSignal) {
			long count = requestSignal.count();
			demandStrategyUpdater.updateAndGet(PublisherSubscription.this,
					demandStrategy1 -> demandStrategy1.applyNewValue(count));

			return SignalVisitResult.CONTINUE;
		}

		@Override
		public SignalVisitResult visit(NextSignal<?> nextSignal) {
			boolean needMore = demandStrategy.needMore();
			if (!needMore) {
				return SignalVisitResult.REQUEUE_AND_PAUSE;
			}

			//noinspection unchecked
			T casted = (T) nextSignal.getItem();
			realSubscriber.publish(casted);
			return SignalVisitResult.CONTINUE;
		}

		@Override
		public SignalVisitResult visit(CancelSignal cancelSignal) {
			publisher.removeSubscription(PublisherSubscription.this);
			realSubscriber.complete(cancelSignal.unsubscription());

			return SignalVisitResult.COMPLETE;
		}

		@Override
		public SignalVisitResult visit(CompleteSignal completeSignal) {
			realSubscriber.complete(completeSignal.completion());

			return SignalVisitResult.COMPLETE;
		}
	}
}
