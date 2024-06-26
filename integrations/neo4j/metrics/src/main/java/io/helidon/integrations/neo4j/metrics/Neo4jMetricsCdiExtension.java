/*
 * Copyright (c) 2021 Oracle and/or its affiliates.
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

package io.helidon.integrations.neo4j.metrics;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.enterprise.inject.spi.Extension;
import org.neo4j.driver.Driver;

import static jakarta.interceptor.Interceptor.Priority.PLATFORM_AFTER;

/**
 * CDI Extension, instantiated by CDI.
 */
public class Neo4jMetricsCdiExtension implements Extension {

    private void addMetrics(@Observes @Priority(PLATFORM_AFTER + 101) @Initialized(ApplicationScoped.class) Object event) {
        Instance<Driver> driver = CDI.current().select(Driver.class);
        Neo4jMetricsSupport.builder()
                .driver(driver.get())
                .build()
                .initialize();
    }
}
