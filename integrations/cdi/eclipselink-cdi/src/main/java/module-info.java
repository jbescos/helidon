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

import io.helidon.common.features.api.Aot;
import io.helidon.common.features.api.Feature;
import io.helidon.common.features.api.HelidonFlavor;

/**
 * Provides classes and interfaces for working with <a
 * href="https://www.eclipse.org/eclipselink/#jpa"
 * target="_parent">Eclipselink</a> in CDI.
 *
 * @see io.helidon.integrations.cdi.eclipselink.CDISEPlatform
 */
@Feature(value = "EclipseLink",
        description = "EclipseLink support for Helidon MP",
        in = HelidonFlavor.MP,
        path = {"JPA", "EclipseLink"}
)
@Aot(false)
module io.helidon.integrations.cdi.eclipselink {
    requires static io.helidon.common.features.api;

    requires java.management;

    requires jakarta.transaction;
    requires jakarta.cdi;
    requires jakarta.inject;
    requires java.sql;
    requires org.eclipse.persistence.jpa;
    requires org.eclipse.persistence.core;

    exports io.helidon.integrations.cdi.eclipselink;

    provides jakarta.enterprise.inject.spi.Extension
            with io.helidon.integrations.cdi.eclipselink.CDISEPlatformExtension;
}
