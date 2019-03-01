package io.titix.kiwi.rx.internal.subject;

import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import io.titix.kiwi.rx.Observable;
import io.titix.kiwi.rx.Subject;
import io.titix.kiwi.rx.Subscription;

/**
 * @author tix32 on 23-Feb-19
 */
public abstract class BaseSubject<T> implements Subject<T> {

	private final AtomicBoolean completed = new AtomicBoolean(false);

	private final Collection<Runnable> completedObservers;

	protected final Collection<Consumer<? super T>> observers;

	protected BaseSubject() {
		this.completedObservers = new ConcurrentLinkedQueue<>();
		this.observers = new ConcurrentLinkedQueue<>();
	}

	public final void next(T object) {
		checkCompleted();
		preNext(object);
		observers.forEach(consumer -> consumer.accept(object));
	}

	@Override
	public final void next(T[] objects) {
		checkCompleted();
		for (T object : objects) {
			preNext(object);
			observers.forEach(observer -> observer.accept(object));
		}
	}

	@Override
	public final void next(Iterable<T> objects) {
		checkCompleted();
		objects.forEach(object -> {
			preNext(object);
			observers.forEach(observer -> observer.accept(object));
		});
	}

	protected void preNext(T object) {
		// do nothing
	}

	@Override
	public void complete() {
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

	public abstract Subscription addObserver(Consumer<? super T> consumer);

	private void checkCompleted() {
		if (completed.get()) {
			throw new IllegalStateException("Subject is completed");
		}
	}
}
