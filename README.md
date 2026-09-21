# Kiwi

**Kiwi** is a lightweight Java reactive streams library. It provides publishers, observables, subscribers, transformation operators, and mutable observable properties for small event-driven flows.

Kiwi is published as `com.github.tix320:kiwi` and uses the JPMS module name `com.github.tix.kiwi`.

## Installation

### Gradle

```groovy
dependencies {
    implementation 'com.github.tix320:kiwi:2.4.0'
}
```

### Maven

```xml
<dependency>
    <groupId>com.github.tix320</groupId>
    <artifactId>kiwi</artifactId>
    <version>3.0.0</version>
</dependency>
```

### Java Modules

```java
module your.module {
    requires com.github.tix.kiwi;
}
```

## What Is Included

- `observable`: observable streams, subscribers, subscriptions, and stream operators.
- `publisher`: publishers for producing stream values.
- `property`: observable mutable values and collection-backed properties.

## Publishers And Observables

A `Publisher<T>` produces values. An `Observable<T>` is the read side that subscribers consume.

```java
Publisher<Integer> publisher = Publisher.simple();
Observable<Integer> observable = publisher.asObservable();

observable
    .map(value -> value * 2)
    .filter(value -> value > 10)
    .subscribe(System.out::println);

publisher.publish(4);
publisher.publish(8);
publisher.complete();
```

Published items must be non-null. Use a null-object value when a stream needs to represent absence.

## Publisher Types

```java
Publisher<String> simple = Publisher.simple();
Publisher<String> single = Publisher.single("initial");
Publisher<String> mono = Publisher.mono();
Publisher<String> buffered = Publisher.buffered(10);
```

- `simple()`: publishes new values only to current subscribers.
- `single(initialValue)`: keeps the last value and sends it to new subscribers.
- `mono()`: publishes one value.
- `buffered(size)`: replays buffered values to new subscribers.

## Subscribing

For simple value handling, subscribe with a `Consumer`.

```java
observable.subscribe(item -> {
    System.out.println(item);
});
```

Use `FlexibleSubscriber<T>` when you need lifecycle callbacks.

```java
observable.subscribe(new FlexibleSubscriber<>() {
    @Override
    public void onNext(String item) {
        if (item.equals("stop")) {
            subscription().cancel();
        }
    }

    @Override
    protected void onError(Throwable error) {
        error.printStackTrace();
    }

    @Override
    public void onComplete(Completion completion) {
        System.out.println(completion);
    }
});
```

`FlexibleSubscriber` requests values automatically. For manual demand control, extend `Subscriber<T>` directly and request items from `onSubscribe`.

```java
observable.subscribe(new Subscriber<>() {
    @Override
    protected void onSubscribe(Subscription subscription) {
        subscription.request(1);
    }

    @Override
    protected void onNext(String item) {
        System.out.println(item);
        subscription().request(1);
    }

    @Override
    protected void onError(Throwable error) {
        error.printStackTrace();
    }

    @Override
    protected void onComplete(Completion completion) {
        System.out.println(completion);
    }
});
```

`onError` receives explicit source failures and is terminal. Exceptions thrown
by subscriber callbacks or operator functions are reported separately as
callback failures and do not terminate the stream.

## Operators

Kiwi observables support common single-stream operators:

```java
Observable<String> names = users
    .filter(User::active)
    .map(User::name)
    .take(5)
    .peek(name -> audit(name));
```

Available operators include:

- `map(...)`
- `filter(...)`
- `peek(...)`
- `take(count)`
- `skip(count)`
- `takeWhile(...)`
- `takeUntil(...)`
- `toMono()`

## Collecting

Collection operators emit their result when the source completes.

```java
Observable<List<String>> list = names.toList();
Observable<Map<String, User>> byName = users.toMap(User::name, user -> user);
Observable<String> joined = names.join(name -> name, ", ");
```

## Combining Streams

```java
Observable<Integer> merged = Observable.merge(first, second);
Observable<Tuple<String, Integer>> zipped = Observable.zip(names, ages);
Observable<Tuple<String, Status>> latest = Observable.combineLatest(names, statuses);
```

- `merge(...)` publishes values from all sources.
- `zip(...)` waits for one value from each source and publishes grouped values.
- `combineLatest(...)` publishes the latest values together after each source has produced at least one value.

## Waiting For Values

Kiwi has blocking helpers for edge cases where a synchronous wait is useful.

```java
Integer value = observable.get(Duration.ofSeconds(5));

observable
    .take(3)
    .await(Duration.ofSeconds(5));
```

Prefer timeout-based methods. The no-timeout `await()` and `get()` methods are deprecated because blocking indefinitely can cause deadlocks.

## Properties

Properties are observable values.

```java
ObjectProperty<String> name = Property.forObject("initial");

name.asObservable().subscribe(System.out::println);
name.setValue("updated");
```

Factory methods are available for common value types:

```java
ObjectProperty<String> object = Property.forObject();
StateProperty<State> state = Property.forState(State.NEW);
ListProperty<String> list = Property.forList(new ArrayList<>());
SetProperty<String> set = Property.forSet(new HashSet<>());
MapProperty<String, Integer> map = Property.forMap(new HashMap<>());
BiMapProperty<String, Integer> biMap = Property.forBiMap(new BiConcurrentHashMap<>());
```

Use `Property.updateAtomic(...)` to update several freezeable properties as one operation.

```java
Property.updateAtomic(firstProperty, secondProperty, () -> {
    firstProperty.setValue("first");
    secondProperty.setValue("second");
});
```

## Scheduler Customization

Kiwi uses its own scheduler internally. Most users should not interact with it directly.

For advanced integration cases, such as routing Kiwi work through an application-owned executor, replace the scheduler with `KiwiSchedulerHolder.changeTo(...)`.

## Versioning

This project follows [Semantic Versioning](https://semver.org/).

## License

**Kiwi** is released under the [Apache License, Version 2.0](LICENSE).

## Contact

- **Author**: [tix320](https://github.com/tix320)
