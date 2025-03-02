package com.github.tix320.kiwi.observable.scheduler;

import java.util.concurrent.TimeUnit;

public interface Scheduler {

	/**
	 * Submits a Runnable task for execution.
	 *
	 * @param task the runnable task
	 */
	void schedule(Runnable task);

	/**
	 * Submits a one-shot task that becomes enabled after the given delay.
	 *
	 * @param delay the time from now to delay execution
	 * @param unit  the time unit of the delay parameter
	 * @param task  the task to execute
	 */
	void schedule(long delay, TimeUnit unit, Runnable task);

	/**
	 * Submits a periodic action that becomes enabled first after the
	 * given initial delay, and subsequently with the given period;
	 * that is, executions will commence after
	 * {@code initialDelay}, then {@code initialDelay + period}, then
	 * {@code initialDelay + 2 * period}, and so on.
	 *
	 * @param initialDelay the time to delay first execution
	 * @param period       the period between successive executions
	 * @param unit         the time unit of the initialDelay and period parameters
	 * @param task         the task to execute
	 */
	void scheduleAtFixedRate(long initialDelay, long period, TimeUnit unit, Runnable task);

	/**
	 * Submits a periodic action that becomes enabled first after the
	 * given initial delay, and subsequently with the given delay
	 * between the termination of one execution and the commencement of
	 * the next.
	 *
	 * @param initialDelay the time to delay first execution
	 * @param delay        the delay between the termination of one
	 *                     execution and the commencement of the next
	 * @param unit         the time unit of the initialDelay and delay parameters
	 * @param task         the task to execute
	 */
	void scheduleWithFixedDelay(long initialDelay, long delay, TimeUnit unit, Runnable task);
}
