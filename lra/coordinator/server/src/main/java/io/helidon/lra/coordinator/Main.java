/*
 * Copyright (c) 2021, 2022 Oracle and/or its affiliates.
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

package io.helidon.lra.coordinator;

import io.helidon.config.Config;
import io.helidon.health.checks.HealthChecks;
import io.helidon.logging.common.LogConfig;
import io.helidon.nima.observe.health.HealthFeature;
import io.helidon.nima.observe.metrics.MetricsFeature;
import io.helidon.nima.webserver.WebServer;
import io.helidon.nima.webserver.http.HttpRouting;

/**
 * In memory Lra coordinator.
 */
public class Main {

    private Main() {
    }

    /**
     * Main method to start Helidon LRA coordinator.
     *
     * @param args are not used
     */
    public static void main(String[] args) {
        LogConfig.configureRuntime();

        Config config = Config.create();

        CoordinatorService coordinatorService = CoordinatorService.builder().build();

        WebServer server = WebServer.builder()
                .routing(it -> updateRouting(it, config, coordinatorService))
                .config(config.get("helidon.lra.coordinator.server"))
                .build();

        String context = config.get("helidon.lra.coordinator.context.path")
                .asString()
                .orElse("/lra-coordinator");

        WebServer webserver = server.start();
        System.out.println("Helidon LRA Coordinator is up! http://localhost:" + webserver.port() + context);
    }

    private static void updateRouting(HttpRouting.Builder routing, Config config, CoordinatorService coordinatorService) {

        MetricsFeature metrics = MetricsFeature.create();
        HealthFeature health = HealthFeature.create(HealthChecks.healthChecks());

        routing.addFeature(metrics)
                .addFeature(health)
                .register(config.get("mp.lra.coordinator.context.path")
                        .asString()
                        .orElse("/lra-coordinator"), coordinatorService)
                .build();
    }
}
