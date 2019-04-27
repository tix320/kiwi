package com.gitlab.tixtix320.kiwi.observable.subject.internal;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import com.gitlab.tixtix320.kiwi.observable.Observable;
import com.gitlab.tixtix320.kiwi.observable.Observer;
import com.gitlab.tixtix320.kiwi.observable.ObserverWithSubscription;
import com.gitlab.tixtix320.kiwi.observable.Subscription;
import com.gitlab.tixtix320.kiwi.observable.internal.SourceObservable;
import com.gitlab.tixtix320.kiwi.observable.subject.Subject;

/**
 * @author Tigran Sargsyan on 23-Feb-19
 */
public abstract class BaseSubject<T> implements Subject<T> {

	private final AtomicBoolean completed = new AtomicBoolean(false);

	private final Collection<Runnable> completedObservers;

	final Collection<Observer<? super T>> observers;

	BaseSubject() {
		this.completedObservers = new ConcurrentLinkedQueue<>();
		this.observers = new ConcurrentLinkedQueue<>();
	}

	public final void next(T object) {
		checkCompleted();
		preNext(object);
		observers.forEach(consumer -> consumer.consume(object));
	}

	@Override
	public final void next(T[] objects) {
		checkCompleted();
		for (T object : objects) {
			preNext(object);
			observers.forEach(observer -> observer.consume(object));
		}
	}

	@Override
	public final void next(Iterable<T> objects) {
		checkCompleted();
		objects.forEach(object -> {
			preNext(object);
			observers.forEach(observer -> observer.consume(object));
		});
	}

	@Override
	public final void complete() {
		checkCompleted();
		completed.set(true);
		completedObservers.forEach(Runnable::run);
		completedObservers.clear();
	}

	@Override
	public final Observable<T> asObservable() {
		return new SourceObservable<>(this);
	}

	public final void onComplete(Runnable runnable) {
		if (completed.get()) {
			runnable.run();
		}
		else {
			this.completedObservers.add(runnable);
		}
	}

	public abstract Subscription addObserver(Observer<? super T> observer);

	public abstract Subscription addObserver(ObserverWithSubscription<? super T> observer);

	protected abstract void preNext(T object);

	private void checkCompleted() {
		if (completed.get()) {
			throw new IllegalStateException("Subject is completed");
		}
	}
}
