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

## Cancellation

A subscription supports non-blocking cancellation through `cancel`.

- `cancel` MUST return without waiting for an in-flight subscriber callback.
- After cancellation is requested, the implementation MUST stop dispatching
  callbacks as soon as it observes cancellation. A callback already executing
  or selected for immediate dispatch MAY finish.
- When cancellation wins the terminal race, the final callback is `onComplete`
  with an `Unsubscription`.
- Cancellation MUST be idempotent. Repeated calls MUST NOT start another
  cancellation operation or produce another terminal callback.
- A subscription cancelled from `onSubscribe` MUST NOT subsequently be
  registered with its observable.

## Multi-source operator subscription

A multi-source operator combines two or more source observables into one
downstream observable. Examples include `combineLatest`, `zip`, and `merge`.

- The downstream `onSubscribe` callback MUST be called only after every source
  observable has supplied its subscription to the operator.
- The operator MUST expose one aggregate subscription to the downstream
  subscriber.
- A request made through the aggregate subscription MUST be propagated to the
  source subscriptions according to the operator's demand semantics.
- Cancellation of the aggregate subscription MUST cancel all active source
  subscriptions as one atomic operator-level action.
- If subscribing to any source fails before the aggregate subscription is
  established, the operator MUST cancel every source subscription already
  obtained and MUST propagate the failure to the caller of `subscribe`.
