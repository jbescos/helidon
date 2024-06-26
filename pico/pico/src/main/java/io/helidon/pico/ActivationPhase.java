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

/**
 * Forms a progression of full activation and deactivation.
 */
public enum ActivationPhase {

    /**
     * Starting state before anything happens activation-wise.
     */
    INIT(false),

    /**
     * Planned to be activated.
     */
    PENDING(true),

    /**
     * Starting to be activated.
     */
    ACTIVATION_STARTING(true),

    /**
     * Gathering dependencies.
     */
    GATHERING_DEPENDENCIES(true),

    /**
     * Constructing.
     */
    CONSTRUCTING(true),

    /**
     * Injecting (fields then methods).
     */
    INJECTING(true),

    /**
     * Calling any post construct method.
     */
    POST_CONSTRUCTING(true),

    /**
     * Finishing post construct method.
     */
    ACTIVATION_FINISHING(true),

    /**
     * Service is active.
     */
    ACTIVE(true),

    /**
     * About to call pre-destroy.
     */
    PRE_DESTROYING(false),

    /**
     * Destroyed (after calling any pre-destroy).
     */
    DESTROYED(false);

    /**
     * True if this phase is eligible for deactivation/shutdown.
     */
    private final boolean eligibleForDeactivation;

    /**
     * Determines whether this phase passes the gate for whether deactivation (PreDestroy) can be called.
     *
     * @return true if this phase is eligible to be included in shutdown processing.
     *
     * @see PicoServices#shutdown()
     */
    public boolean eligibleForDeactivation() {
        return eligibleForDeactivation;
    }

    ActivationPhase(boolean eligibleForDeactivation) {
        this.eligibleForDeactivation = eligibleForDeactivation;
    }
}
