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
- The subscription supplied to `onSubscribe` MUST NOT be `null`.
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
- After unbounded demand has been requested, later positive finite requests
  MUST be safe no-ops and MUST NOT notify the underlying subscription.
- A source MUST NOT call `onNext` more times than requested. Operators that
  consume an item without forwarding it MUST replace that unit of upstream
  demand.
- Terminal completion is not subject to item demand. Once a source has no
  buffered items left to drain, it MUST deliver completion without waiting for
  another request.
- An operator that needs the complete source to produce one result MUST consume
  the source only after downstream demand exists and MUST emit at most one
  result.

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
- A completion signal and its completion value MUST NOT be `null`.
- If `onNext` throws an `Exception`, the failure MUST be reported to the
  scheduler's uncaught-exception path and remaining queued signals MUST remain
  eligible for processing.
- A terminal callback is the final callback for its subscription. Its token and
  remaining signals MUST be removed even if the callback throws an `Exception`.
- A callback exception MUST NOT leave already-queued signals for other active
  subscriptions permanently unscheduled.
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
- A cancellation reason MUST NOT be `null`.
- A subscription cancelled from `onSubscribe` MUST NOT remain registered after
  `onSubscribe` returns.

## Property freezing

- `freeze` and `unfreeze` MAY be nested. A frozen value is released only by the
  matching outermost `unfreeze` call.
- Calling `unfreeze` without a matching `freeze` MUST be a safe no-op.
- Calling `unfreeze` after the property has been closed MUST NOT throw merely
  because the pending frozen value can no longer be published. That pending
  publication is discarded.
- An atomic property update batches publications; it does not roll back
  mutations when its action fails.
- A mutable collection property MUST retain and mutate its supplied collection.
  Its current value is that collection, while values delivered to subscribers
  MUST be unmodifiable views rather than defensive snapshots.

## Publisher termination

- A publisher MUST reject `publish`, `complete`, and `abort` after it has been
  closed.
- Completion values and explicitly published failures MUST NOT be `null`.
- Any transformation required to prepare a published value MUST succeed before
  the new current and observable state is committed. A preparation failure MUST
  leave the previous state intact.
- Source completion MUST drain already accepted buffered items according to
  demand before delivering `onComplete`.
- Abort and error delivery are intentionally not specified yet. They will be
  defined together with the subscriber error callback contract.

## Multi-source operator subscription

A multi-source operator combines two or more source observables into one
downstream observable.

- The downstream `onSubscribe` callback MUST be called only after every source
  observable has supplied its subscription to the operator.
- The operator MUST expose one aggregate subscription to the downstream
  subscriber.
- A request made through the aggregate subscription MUST be propagated to the
  source subscriptions according to the operator's demand semantics.
- If one source request throws an `Exception`, the operator MUST still attempt
  the request on every remaining source and then propagate the first failure.
  Later failures MUST be attached to it as suppressed exceptions.
- Cancellation of the aggregate subscription MUST be propagated to every
  active source before the aggregate `cancel` call returns.
- If one source cancellation throws an `Exception`, the operator MUST still
  attempt every remaining source cancellation and then propagate the failure.
- A cancellation failure MUST NOT prevent an already-decided downstream item
  or terminal callback from being attempted. If both the downstream action and
  cancellation fail, the downstream failure is primary and the cancellation
  failure is suppressed.
- If subscribing to any source fails before the aggregate subscription is
  established, the operator MUST cancel every source subscription already
  obtained and MUST propagate the failure to the caller of `subscribe`.
- An unsupported demand mode MUST fail synchronously instead of being silently
  ignored.

## Blocking helpers

- A negative timeout means wait indefinitely. A non-negative timeout bounds the
  wait in milliseconds.
- Positive timeout fractions smaller than one millisecond MUST be rounded up to
  the next millisecond.
- If an item or completion wins the terminal race before the timeout, the
  blocking operation MUST wait for that winning callback to finish.
- `get` MUST return the first item, or `null` when the observable completes
  without an item.
- A timeout or interruption MUST request cancellation before throwing. That
  cancellation is non-blocking and follows the normal cancellation race rules.

## Timeout fallback

- A timeout fallback starts its clock when the fallback observable is created,
  not when that observable is later subscribed.
- The first item from either the source or fallback wins and the other source
  MUST be cancelled.
