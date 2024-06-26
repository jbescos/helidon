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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

import io.helidon.common.LazyValue;

import static io.helidon.nima.faulttolerance.FaultTolerance.toDelayedRunnable;
import static io.helidon.nima.faulttolerance.SupplierHelper.toRuntimeException;
import static io.helidon.nima.faulttolerance.SupplierHelper.unwrapThrowable;

class TimeoutImpl implements Timeout {
    private static final System.Logger LOGGER = System.getLogger(TimeoutImpl.class.getName());

    private final long timeoutMillis;
    private final LazyValue<? extends ExecutorService> executor;
    private final boolean currentThread;
    private final String name;

    TimeoutImpl(Builder builder) {
        this.timeoutMillis = builder.timeout().toMillis();
        this.executor = builder.executor();
        this.currentThread = builder.currentThread();
        this.name = builder.name();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public <T> T invoke(Supplier<? extends T> supplier) {
        if (!currentThread) {
            try {
                return CompletableFuture.supplyAsync(supplier, executor.get())
                        .orTimeout(timeoutMillis, TimeUnit.MILLISECONDS)
                        .get();
            } catch (Throwable t) {
                throw mapThrowable(t, null);
            }
        } else {
            Thread thisThread = Thread.currentThread();
            ReentrantLock interruptLock = new ReentrantLock();
            AtomicBoolean callReturned = new AtomicBoolean(false);
            AtomicBoolean interrupted = new AtomicBoolean(false);

            executor.get().submit(toDelayedRunnable(() -> {
                interruptLock.lock();
                try {
                    if (callReturned.compareAndSet(false, true)) {
                        thisThread.interrupt();
                        interrupted.set(true);      // needed if InterruptedException caught in supplier
                    }
                } finally {
                    interruptLock.unlock();
                }
            }, timeoutMillis));

            try {
                T result = supplier.get();
                if (interrupted.get()) {
                    throw new TimeoutException("Supplier execution interrupted", null);
                }
                return result;
            } catch (Throwable t) {
                throw mapThrowable(t, interrupted);
            } finally {
                interruptLock.lock();
                try {
                    callReturned.set(true);
                    // Run invocation in current thread
                    // Clear interrupted flag here -- required for uninterruptible busy loops
                    if (Thread.interrupted()) {
                        LOGGER.log(System.Logger.Level.DEBUG, "Current thread interrupted, clearing status");
                    }
                } finally {
                    interruptLock.unlock();
                }
            }
        }
    }

    private static RuntimeException mapThrowable(Throwable t, AtomicBoolean interrupted) {
        Throwable throwable = unwrapThrowable(t);
        if (throwable instanceof InterruptedException) {
            return new TimeoutException("Call interrupted", throwable);

        } else if (throwable instanceof java.util.concurrent.TimeoutException) {
            return new TimeoutException("Timeout reached", throwable.getCause());
        } else if (interrupted != null && interrupted.get()) {
            return new TimeoutException("Supplier execution interrupted", t);
        }
        return toRuntimeException(throwable);
    }
}
