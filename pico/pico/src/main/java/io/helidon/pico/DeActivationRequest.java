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

import io.helidon.builder.Builder;
import io.helidon.config.metadata.ConfiguredOption;

/**
 * Request to {@link io.helidon.pico.DeActivator#deactivate(DeActivationRequest)}.
 *
 * @param <T> type to deactivate
 */
@Builder
public interface DeActivationRequest<T> {

    /**
     * Create a request with defaults.
     *
     * @param provider service provider responsible for invoking deactivate
     * @return a new request
     * @param <T> type to deactivate
     */
    @SuppressWarnings("unchecked")
    static <T> DeActivationRequest<T> create(ServiceProvider<T> provider) {
        return DefaultDeActivationRequest.builder().serviceProvider(provider).build();
    }

    /**
     * Service provider responsible for invoking deactivate.
     *
     * @return service provider
     */
    ServiceProvider<T> serviceProvider();

    /**
     * Whether to throw an exception on failure, or return it as part of the result.
     *
     * @return throw on failure
     */
    @ConfiguredOption("true")
    boolean throwOnFailure();

}
