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

package io.helidon.builder.test.testsubjects;

import java.util.Optional;

import io.helidon.builder.Builder;
import io.helidon.config.metadata.ConfiguredOption;

/**
 * Demonstrates multi-level inheritance for the generated builder.
 *
 * @see Level2
 */
@Builder(requireLibraryDependencies = false, requireBeanStyle = true, implPrefix = "", implSuffix = "Impl")
public interface Level1 extends Level0 {

    /**
     * Used for testing and demonstrating default values on the generated builder.
     *
     * @return ignored, here for testing purposes only
     */
    @Override
    @ConfiguredOption("1")
    String getLevel0StringAttribute();

    /**
     * Used for testing and demonstrating default values on the generated builder.
     *
     * @return ignored, here for testing purposes only
     */
    @ConfiguredOption("1")
    int getLevel1intAttribute();

    /**
     * Used for testing and demonstrating default values on the generated builder.
     *
     * @return ignored, here for testing purposes only
     */
    @ConfiguredOption("1")
    Integer getLevel1IntegerAttribute();

    /**
     * Used for testing and demonstrating default values on the generated builder.
     *
     * @return ignored, here for testing purposes only
     */
    @ConfiguredOption("true")
    boolean getLevel1booleanAttribute();

    /**
     * Used for testing and demonstrating usage.
     *
     * @return ignored, here for testing purposes only
     */
    Optional<Boolean> getLevel1BooleanAttribute();

}
