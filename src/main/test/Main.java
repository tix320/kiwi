import io.titix.kiwi.observable.Observable;
import io.titix.kiwi.observable.Subject;
import io.titix.kiwi.observable.Subscription;
import io.titix.kiwi.observable.internal.DefaultSubject;

/**
 * @author tix32 on 21-Feb-19
 */
public class Main {

	public static void main(String[] args) {
		Subject<Integer> defaultSubject = Subject.create();

		Observable<Integer> observable = defaultSubject.asObservable();
		observable.subscribe(o -> System.out.println(o+1));
		defaultSubject.next(10);
		System.out.println();
		Subscription subscribe = observable.subscribe(o -> System.out.println(o + 2));
		defaultSubject.next(10);
		System.out.println();
		observable.subscribe(o -> System.out.println(o+3));
		subscribe.unsubscribe();
		defaultSubject.next(10);
		System.out.println();
		observable.subscribe(o -> System.out.println(o+4));
		defaultSubject.next(10);

	}
}
