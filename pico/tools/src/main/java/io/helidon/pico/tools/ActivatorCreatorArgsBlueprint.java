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

package io.helidon.pico.tools;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.helidon.builder.api.Prototype;
import io.helidon.common.types.TypeName;
import io.helidon.pico.api.DependenciesInfo;
import io.helidon.pico.api.ServiceInfoBasics;

/**
 * See {@link ActivatorCreatorDefault}.
 */
@Prototype.Blueprint(isPublic = false)
interface ActivatorCreatorArgsBlueprint {
    String constructor();
    String template();
    TypeName serviceTypeName();
    TypeName activatorTypeName();
    Optional<String> activatorGenericDecl();
    Optional<TypeName> parentTypeName();
    Set<TypeName> scopeTypeNames();
    List<String> description();
    ServiceInfoBasics serviceInfo();
    Optional<DependenciesInfo> dependencies();
    Optional<DependenciesInfo> parentDependencies();
    Collection<Object> injectionPointsSkippedInParent();
    List<Object> serviceTypeInjectionOrder();
    String generatedSticker();
    Optional<Double> weightedPriority();
    Optional<Integer> runLevel();
    Optional<String> postConstructMethodName();
    Optional<String> preDestroyMethodName();
    List<String> extraCodeGen();
    List<String> extraClassComments();
    boolean isConcrete();
    boolean isProvider();
    boolean isSupportsJsr330InStrictMode();
}
