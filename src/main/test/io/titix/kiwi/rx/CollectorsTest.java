package io.titix.kiwi.rx;

import org.junit.jupiter.api.Test;

/**
 * @author Tigran.Sargsyan on 28-Feb-19
 */
class CollectorsTest {

	@Test
	void toMapTest(){
		Observable.of("a","aa","aaa","aaaa").toMap(s -> s.length(),s->s).subscribe(integerStringMap -> System.out.println(integerStringMap));
	}
}
