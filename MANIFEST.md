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
- The subscriber MUST NOT receive an item, completion, or error signal before
  `onSubscribe` has completed successfully.
- If `onSubscribe` throws, the subscriber MUST NOT be registered with the
  observable. The failure MUST be propagated to the caller of `subscribe`.
- A `Subscriber` instance accepts only one subscription. Reusing it for another
  subscription MUST fail.
- Configuration objects and callbacks retained for later asynchronous use MUST
  reject `null` when the operation is configured, rather than failing later
  during signal processing.
- Mutable state that controls one subscription MUST NOT be shared with another
  subscription to the same observable unless that sharing is an explicit part
  of the observable's contract.

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
- When a user-supplied operator function fails while processing an item, the
  operator MUST replace that unit of upstream demand before reporting the
  callback failure.
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
- Cancellation MUST take priority over queued errors, items, and
  source-completion signals. Errors MUST take priority over queued items. Queued
  items MUST take priority over source completion so accepted buffered items
  are drained before `onComplete`.
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

## Error termination

- An explicit source failure MUST be delivered through `onError` with the
  original failure instance.
- `onError` is terminal and mutually exclusive with `onComplete`. No callback
  may follow it for the same subscription.
- Competing completion, error, and cancellation attempts MUST produce exactly
  one terminal callback for a subscription.
- An `Exception` thrown by a subscriber callback or a user-supplied operator
  function is a callback failure. It MUST be reported through the scheduler's
  uncaught-exception path and MUST NOT be converted into `onError`. Later
  signals MUST remain eligible for processing.
- If a terminal aggregation callback fails, the operator MUST still attempt
  downstream completion before reporting the callback failure.
- When one source of a multi-source operation fails, the operation MUST cancel
  every active source and terminate downstream through `onError` exactly once.
- An error value MUST NOT be `null`.

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
- An atomic property update MUST unfreeze every property it successfully froze,
  including when a later freeze or the update action fails. Cleanup MUST be
  attempted for every such property.
- If both an atomic update action and cleanup fail, the action failure MUST
  remain primary and cleanup failures MUST be attached as suppressed
  exceptions.
- Every mutating operation on a closed property MUST fail with the property's
  closed-state exception, regardless of whether the mutation replaces the
  value or modifies a retained collection.
- A mutable collection property MUST retain and mutate its supplied collection
  rather than replacing it with defensive copies.
- Whether subscribers receive a mutable or read-only view is part of the
  property's declared observable type and MUST NOT be disguised behind a type
  that promises mutation support.

## Publisher termination

- A publisher MUST reject `publish`, `complete`, and `abort` after it has been
  closed.
- Publisher completion and abort are mutually exclusive. If they race, exactly
  one terminal state MUST win.
- Completion values and explicitly published failures MUST NOT be `null`.
- Any transformation required to prepare a published value MUST succeed before
  the new current and observable state is committed. A preparation failure MUST
  leave the previous state intact.
- Source completion MUST drain already accepted buffered items according to
  demand before delivering `onComplete`.
- Aborting a publisher MUST terminate current and future subscribers through
  `onError` with the supplied failure.

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
- If the observable terminates with an error, a blocking helper MUST throw that
  original failure.

## Timeout fallback

- A timeout fallback starts its clock when the fallback observable is created,
  not when that observable is later subscribed.
- A timeout duration MUST NOT be negative. Positive sub-millisecond durations
  MUST retain their precision rather than being truncated to zero.
- Each subscription MUST have independent timeout and cancellation state. One
  subscriber's source result or cancellation MUST NOT disable another
  subscriber's fallback.
- The first item from either the source or fallback wins and the other source
  MUST be cancelled.
- If the source wins before the fallback value factory starts, that factory
  MUST NOT be invoked.
