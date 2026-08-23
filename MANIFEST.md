# Kiwi Manifest

This document defines the behavioral contracts of Kiwi's reactive components.
It is both a specification for implementations and a reference for tests.

The key words **MUST**, **MUST NOT**, **SHOULD**, and **SHOULD NOT** describe
requirements on an implementation.

## Observable subscription

When an `Observable` receives a call to `subscribe(Subscriber)`, it establishes
the subscription through the subscriber's `onSubscribe` callback.

- `onSubscribe` MUST be called synchronously, on the same thread that called
  `subscribe`, before `subscribe` returns.
- `onSubscribe` MUST be called exactly once for a subscriber.
- The subscriber MUST NOT receive an item or a completion signal before
  `onSubscribe` has completed successfully.
- If `onSubscribe` throws, the subscriber MUST NOT be registered with the
  observable. The failure MUST be propagated to the caller of `subscribe`.
- A `Subscriber` instance accepts only one subscription. Reusing it for another
  subscription MUST fail.

## Items and demand

- An observable or publisher MUST NOT emit `null`. A null item MUST be rejected
  before it reaches `onNext`.
- `request(n)` MUST reject non-positive values.
- Finite demand MUST be additive and MUST saturate at `Long.MAX_VALUE` instead
  of overflowing. `requestUnbounded()` is equivalent to unbounded demand.
- `requestUnbounded()` MUST be idempotent. After its first invocation, later
  invocations MUST NOT notify the underlying subscription again.
- A source MUST NOT call `onNext` more times than requested. Operators that
  consume an item without forwarding it, such as `filter` and `skip`, MUST
  replace that unit of upstream demand.
- An operator that needs the complete source to produce one result, such as a
  collector, MUST consume the source only after downstream demand exists and
  MUST emit at most one result.

## Signal ordering

- Subscriber callbacks within one pipeline MUST be serialized.
- Signals from the same source with equal priority MUST be delivered in creation
  order.
- Cancellation MUST take priority over queued item and source-completion
  signals. Queued items MUST take priority over source completion so accepted
  buffered items are drained before `onComplete`.
- Across multiple sources, the synchronizer MUST select the oldest current
  source-head signal after applying each source's local priority ordering.
- A terminal signal MUST remove its token and all remaining queued signals.
- If an ordinary callback throws an `Exception`, the failure MUST be reported
  to the scheduler's uncaught-exception path and remaining queued signals MUST
  remain eligible for processing.
- The library MUST NOT catch or wrap `Error` instances.

## Cancellation

A subscription supports cancellation through `cancel` without exposing a
completion future or a blocking cancellation barrier.

- `cancel` MUST return without waiting for an in-flight subscriber callback.
- After cancellation is requested, the implementation MUST stop dispatching
  callbacks as soon as it observes cancellation. A callback already executing
  or selected for immediate dispatch MAY finish.
- When cancellation wins the terminal race, the final callback is `onComplete`
  with an `Unsubscription`.
- Cancellation MUST be idempotent. Repeated calls MUST NOT start another
  cancellation operation or produce another terminal callback.
- A subscription cancelled from `onSubscribe` MUST NOT remain registered after
  `onSubscribe` returns.

## Publisher termination

- A publisher MUST reject `publish`, `complete`, and `abort` after it has been
  closed.
- Source completion MUST drain already accepted buffered items according to
  demand before delivering `onComplete`.
- Abort and error delivery are intentionally not specified yet. They will be
  defined together with the subscriber error callback contract.

## Multi-source operator subscription

A multi-source operator combines two or more source observables into one
downstream observable. Examples include `combineLatest`, `zip`, and `merge`.

- The downstream `onSubscribe` callback MUST be called only after every source
  observable has supplied its subscription to the operator.
- The operator MUST expose one aggregate subscription to the downstream
  subscriber.
- A request made through the aggregate subscription MUST be propagated to the
  source subscriptions according to the operator's demand semantics.
- Cancellation of the aggregate subscription MUST be propagated to every
  active source before the aggregate `cancel` call returns.
- If one source cancellation throws an `Exception`, the operator MUST still
  attempt every remaining source cancellation and then propagate the failure.
- If subscribing to any source fails before the aggregate subscription is
  established, the operator MUST cancel every source subscription already
  obtained and MUST propagate the failure to the caller of `subscribe`.
