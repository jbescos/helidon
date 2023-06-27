/*
 * Copyright (c) 2023 Oracle and/or its affiliates.
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

import io.helidon.pico.api.Contract;

/**
 * A generated service to support circuit breaker without resorting to Class.forName() for exception types.
 * @deprecated only for generated code
 */
@Contract
@Deprecated
public interface CircuitBreakerMethod extends FtMethod {
    /**
     * Provide a circuit breaker instance that should be used with this method.
     * If the {@link io.helidon.nima.faulttolerance.FaultTolerance.CircuitBreaker} annotation contains a name, we will attempt to obtain the named instance from
     * registry. If such a named instance does not exist a new circuit breaker will be created from the annotation.
     *
     * @return circuit breaker instance
     */
    CircuitBreaker circuitBreaker();
}
