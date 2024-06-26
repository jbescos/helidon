/*
 * Copyright (c) 2022 Oracle and/or its affiliates.
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

package io.helidon.pico;

import java.util.Optional;
import java.util.Set;

import io.helidon.builder.Builder;
import io.helidon.builder.Singular;

/**
 * A criteria to discover service.
 */
@Builder
public interface ServiceInfoCriteria {

    /**
     * The managed service implementation {@link Class}.
     *
     * @return the service type name
     */
    Optional<String> serviceTypeName();

    /**
     * The managed service assigned Scope's.
     *
     * @return the service scope type name
     */
    @Singular
    Set<String> scopeTypeNames();

    /**
     * The managed service assigned Qualifier's.
     *
     * @return the service qualifiers
     */
    @Singular
    Set<QualifierAndValue> qualifiers();

    /**
     * The managed services advertised types (i.e., typically its interfaces).
     *
     * @see io.helidon.pico.ExternalContracts
     * @return the service contracts implemented
     */
    @Singular
    Set<String> contractsImplemented();

    /**
     * The optional {@link RunLevel} ascribed to the service.
     *
     * @return the service's run level
     */
    Optional<Integer> runLevel();

    /**
     * Weight that was declared on the type itself.
     *
     * @return the declared weight
     */
    Optional<Double> weight();

    /**
     * The managed services external contracts / interfaces. These should also be contained within
     * {@link #contractsImplemented()}. External contracts are from other modules other than the module containing
     * the implementation typically.
     *
     * @see io.helidon.pico.ExternalContracts
     * @return the service external contracts implemented
     */
    @Singular
    Set<String> externalContractsImplemented();

    /**
     * The management agent (i.e., the activator) that is responsible for creating and activating - typically build-time created.
     *
     * @return the activator type name
     */
    Optional<String> activatorTypeName();

    /**
     * The name of the ascribed module, if known.
     *
     * @return the module name
     */
    Optional<String> moduleName();

}
