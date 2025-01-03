/*
 * Copyright (c) 2023, 2024 Oracle and/or its affiliates.
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

package io.helidon.service.tests.toolbox.impl;

import java.io.Serializable;

import io.helidon.service.registry.Service;
import io.helidon.service.tests.toolbox.SomeOtherLocalNonContractInterface1;
import io.helidon.service.tests.toolbox.Tool;

@Service.Singleton
public class Screwdriver implements Tool, SomeOtherLocalNonContractInterface1, Serializable {

    @Override
    public String name() {
        return "screwdriver";
    }

}