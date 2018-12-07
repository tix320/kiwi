package test;

import java.time.zone.ZoneRulesException;
import java.util.Objects;
import java.util.function.Predicate;

import de.fsyo.uremn.check.Try;

public class Main {

	public static void main(String[] args) {
		Main dsdas = Try.success(new A())
				.filter(a -> false)
				.map(a -> new Main())
				.getOrElseThrow(() -> new ZoneRulesException("dsdas"));
		System.out.println(dsdas);
	}

	public static boolean isNull(Main obj) {
		return obj == null;
	}


}

class A extends Main {

}