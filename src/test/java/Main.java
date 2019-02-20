import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.titix.kiwi.util.promise.Promise;

/**
 * @author Tigran.Sargsyan on 20-Feb-19
 */
public class Main {

	public static void main(String[] args) {
		new Promise<>((accept, reject) -> {
			Executors.newSingleThreadScheduledExecutor().schedule(() -> accept.accept("hello"),2, TimeUnit.SECONDS);

		}).then(o -> {
			System.out.println(o);
		});


	}
}
