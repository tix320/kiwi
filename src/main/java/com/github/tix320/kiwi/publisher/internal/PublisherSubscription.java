package com.github.tix320.kiwi.publisher.internal;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import com.github.tix320.kiwi.observable.Subscriber;
import com.github.tix320.kiwi.observable.Subscription;
import com.github.tix320.kiwi.observable.Unsubscription;
import com.github.tix320.kiwi.observable.demand.DemandStrategy;
import com.github.tix320.kiwi.observable.demand.EmptyDemandStrategy;
import com.github.tix320.kiwi.observable.demand.InfiniteDemandStrategy;
import com.github.tix320.kiwi.observable.signal.*;
import com.github.tix320.kiwi.observable.signal.SignalManager.SignalVisitResult;

public final class PublisherSubscription<T> extends Subscription {

	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<PublisherSubscription, DemandStrategy> DEMAND_STRATEGY = AtomicReferenceFieldUpdater.newUpdater(
			PublisherSubscription.class, DemandStrategy.class, "demandStrategy");
	private static final VarHandle WIP;

	static {
		try {
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			WIP = lookup.findVarHandle(PublisherSubscription.class, "wip", boolean.class);
		}
		catch (ReflectiveOperationException e) {
			throw new ExceptionInInitializerError(e);
		}
	}

	private final BasePublisher<T> publisher;

	private final Subscriber<? super T> realSubscriber;

	private final SignalManager.Token token;

	private final PublisherCursor publisherCursor;

	private final PublisherSignalVisitor publisherSignalVisitor = new PublisherSignalVisitor();

	private volatile boolean wip;
	private volatile DemandStrategy demandStrategy = EmptyDemandStrategy.INSTANCE;

	public PublisherSubscription(BasePublisher<T> publisher, Subscriber<? super T> realSubscriber,
								 PublisherCursor publisherCursor) {
		this.publisher = publisher;
		this.realSubscriber = realSubscriber;

		this.token = realSubscriber.getSignalManager().createToken(new SignalManagerVisitor());
		this.publisherCursor = publisherCursor;
	}

	public void start() {
		token.start();
	}

	public void doAction() {
		boolean changed = WIP.compareAndSet(this, false, true);
		if (changed) {

			Signal signal = publisherCursor.get();
			if (signal != null) {
				signal.accept(publisherSignalVisitor);
			}

			boolean c = WIP.compareAndSet(this, true, false);
			if (!c) {
				throw new IllegalStateException();
			}
		}
	}

	@Override
	protected void onRequest(long count) {
		DemandStrategy previousStrategy = DEMAND_STRATEGY.getAndUpdate(PublisherSubscription.this,
				strategy -> strategy.addBound(count));

		if (!previousStrategy.needMore()) {
			doAction();// TODO why in if
		}
	}

	@Override
	protected void onUnboundRequest() {
		DemandStrategy previousStrategy = DEMAND_STRATEGY.getAndSet(PublisherSubscription.this,
				InfiniteDemandStrategy.INSTANCE);

		if (!previousStrategy.needMore()) {
			doAction();// TODO why in if
		}
	}

	@Override
	protected void onCancel(Unsubscription unsubscription) {
		CancelSignal cancelSignal = new CancelSignal(unsubscription);

		token.addSignal(cancelSignal);

		token.tryRunWorker();
	}

	private final class PublisherSignalVisitor extends AbstractSignalVisitor<Void> {

		@Override
		public Void visit(PublishSignal<?> publishSignal) {
			if (demandStrategy.next()) {
				token.addSignal(publishSignal);
				publisherCursor.moveForward();
			}

			return null;
		}

		@Override
		public Void visit(CompleteSignal completeSignal) {
			token.addSignal(completeSignal);
			publisherCursor.moveForward();

			return null;
		}
	}

	private final class SignalManagerVisitor implements SignalVisitor<SignalVisitResult> {

		@Override
		public SignalVisitResult visit(PublishSignal<?> publishSignal) {
			//noinspection unchecked
			T casted = (T) publishSignal.getItem();
			try {
				realSubscriber.publish(casted);
				doAction();
			}
			catch (Subscriber.UserCallbackException e) {
				token.addSignal(new ErrorSignal(e.getCause()));
			}

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

		@Override
		public SignalVisitResult visit(ErrorSignal errorSignal) {
			realSubscriber.completeWithError(errorSignal.error());

			return SignalVisitResult.COMPLETE;
		}
	}
}
