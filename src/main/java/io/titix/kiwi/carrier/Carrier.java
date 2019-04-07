package io.titix.kiwi.carrier;

public interface Carrier<T, R> {

	R deliver(T value);

	Recipient<T, R> newRecipient();
}
