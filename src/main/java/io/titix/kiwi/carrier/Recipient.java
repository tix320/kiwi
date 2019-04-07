package io.titix.kiwi.carrier;

import java.util.function.Function;

public interface Recipient<T, R> {

	void subscribe(Function<T, R> respondent);
}
