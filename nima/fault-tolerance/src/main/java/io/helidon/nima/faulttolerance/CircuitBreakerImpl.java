/*
 * Copyright (c) 2020, 2022 Oracle and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.helidon.nima.faulttolerance;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import io.helidon.common.LazyValue;

import static io.helidon.nima.faulttolerance.FaultTolerance.toDelayedCallable;
import static io.helidon.nima.faulttolerance.SupplierHelper.toRuntimeException;
import static io.helidon.nima.faulttolerance.SupplierHelper.unwrapThrowable;

class CircuitBreakerImpl implements CircuitBreaker {
    /*
     Configuration options
     */
    private final LazyValue<? extends ExecutorService> executor;
    // how long to transition from open to half-open
    private final long delayMillis;
    // how many successful calls will close a half-open breaker
    private final int successThreshold;

    /*
    Runtime
     */
    private final AtomicReference<State> state = new AtomicReference<>(State.CLOSED);
    // rolling window for counting errors to (maybe) open the breaker
    private final ResultWindow results;
    // to close from half-open
    private final AtomicInteger successCounter = new AtomicInteger();
    private final AtomicBoolean halfOpenInProgress = new AtomicBoolean();
    private final AtomicReference<Future<Boolean>> schedule = new AtomicReference<>();
    private final ErrorChecker errorChecker;
    private final String name;

    CircuitBreakerImpl(Builder builder) {
        this.delayMillis = builder.delay().toMillis();
        this.successThreshold = builder.successThreshold();
        this.results = new ResultWindow(builder.volume(), builder.errorRatio());
        this.executor = builder.executor();
        this.errorChecker = ErrorChecker.create(builder.skipOn(), builder.applyOn());
        this.name = builder.name();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public <T> T invoke(Supplier<? extends T> supplier) {
        return switch (state.get()) {
            case CLOSED -> executeTask(supplier);
            case HALF_OPEN -> halfOpenTask(supplier);
            case OPEN -> throw new CircuitBreakerOpenException("CircuitBreaker is open");
        };
    }

    @Override
    public State state() {
        return state.get();
    }

    @Override
    public void state(State newState) {
        if (newState == State.CLOSED) {
            if (state.get() == State.CLOSED) {
                // fine
                resetCounters();
                return;
            }

            Future<Boolean> future = schedule.getAndSet(null);
            if (future != null) {
                future.cancel(false);
            }
            resetCounters();
            state.set(State.CLOSED);
        } else if (newState == State.OPEN) {
            state.set(State.OPEN);
            Future<Boolean> future = schedule.getAndSet(null);
            if (future != null) {
                future.cancel(false);
            }
            resetCounters();
        } else {
            // half open
            resetCounters();
        }
    }

    private <U> U executeTask(Supplier<? extends U> supplier) {
        try {
            U result = supplier.get();
            results.update(ResultWindow.Result.SUCCESS);
            return result;
        } catch (Throwable t) {
            Throwable throwable = unwrapThrowable(t);
            if (errorChecker.shouldSkip(throwable)) {
                results.update(ResultWindow.Result.SUCCESS);
            } else {
                results.update(ResultWindow.Result.FAILURE);
            }
            throw toRuntimeException(throwable);
        } finally {
            if (results.shouldOpen() && state.compareAndSet(State.CLOSED, State.OPEN)) {
                results.reset();
                // if we successfully switch to open, we need to schedule switch to half-open
                scheduleHalf();
            }
        }
    }

    private <U> U halfOpenTask(Supplier<? extends U> supplier) {
        // half-open
        if (halfOpenInProgress.compareAndSet(false, true)) {
            try {
                U result = supplier.get();
                // success
                int successes = successCounter.incrementAndGet();
                if (successes >= successThreshold) {
                    // transition to closed
                    successCounter.set(0);
                    state.compareAndSet(State.HALF_OPEN, State.CLOSED);
                }
                return result;
            } catch (Throwable t) {
                Throwable throwable = unwrapThrowable(t);
                if (errorChecker.shouldSkip(throwable)) {
                    // success
                    int successes = successCounter.incrementAndGet();
                    if (successes >= successThreshold) {
                        // transition to closed
                        successCounter.set(0);
                        state.compareAndSet(State.HALF_OPEN, State.CLOSED);
                    }
                } else {
                    // failure
                    successCounter.set(0);
                    state.set(State.OPEN);
                    // if we successfully switch to open, we need to schedule switch to half-open
                    scheduleHalf();
                }
                throw toRuntimeException(throwable);
            } finally {
                halfOpenInProgress.set(false);
            }
        } else {
            throw new CircuitBreakerOpenException("CircuitBreaker is half open, parallel execution in progress");
        }
    }

    private void scheduleHalf() {
        schedule.set(executor.get().submit(
                toDelayedCallable(() -> {
                    state.compareAndSet(State.OPEN, State.HALF_OPEN);
                    schedule.set(null);
                    return true;
                }, delayMillis)));
    }

    Future<Boolean> schedule() {
        return schedule.get();
    }

    private void resetCounters() {
        results.reset();
        successCounter.set(0);
    }
}
