package com.github.tix320.kiwi.property;

@FunctionalInterface
public interface CheckedAction {

	void run() throws Exception;

}
