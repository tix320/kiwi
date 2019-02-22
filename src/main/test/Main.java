import io.titix.kiwi.observable.Observable;
import io.titix.kiwi.observable.Subject;

/**
 * @author tix32 on 21-Feb-19
 */
public class Main {

	public static void main(String[] args) {
		Subject<Integer> defaultSubject = Subject.create();

		Observable<Integer> observable = defaultSubject.asObservable();
		observable.subscribe(o -> System.out.println("lol " + o));
		defaultSubject.next(10);

		observable.take(4).subscribe(integer -> System.out.println("hey " + integer));

		observable.one().subscribe(integer -> System.out.println("mihat" + integer));

		defaultSubject.next(10);
		defaultSubject.next(10);
		defaultSubject.next(10);
		defaultSubject.next(10);
		defaultSubject.next(10);
		defaultSubject.next(10);
		defaultSubject.next(10);

	}
}
