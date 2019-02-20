package io.titix.kiwi.util.promise;

/**
 * @author Tigran.Sargsyan on 20-Feb-19
 */
public interface PromiseResolver<T> {

	void resolve(Accept<T> accept, Reject reject);
}
