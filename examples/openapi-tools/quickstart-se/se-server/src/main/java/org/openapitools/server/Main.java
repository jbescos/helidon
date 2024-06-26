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

package org.openapitools.server;

import org.openapitools.server.api.MessageServiceImpl;

import io.helidon.common.LogConfig;
import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.health.HealthSupport;
import io.helidon.health.checks.HealthChecks;
import io.helidon.media.jsonp.JsonpSupport;
import io.helidon.media.jackson.JacksonSupport;
import org.openapitools.server.api.JsonProvider;
import io.helidon.metrics.MetricsSupport;
import io.helidon.openapi.OpenAPISupport;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

/**
 * The application main class.
 */
public final class Main {

    /**
     * Cannot be instantiated.
     */
    private Main() {
    }

    /**
     * Application main entry point.
     *
     * @param args command line arguments.
     */
    public static void main(final String[] args) {
        startServer();
    }

    /**
     * Start the server.
     *
     * @return the created {@link WebServer} instance
     */
    static Single<WebServer> startServer() {

        // load logging configuration
        LogConfig.configureRuntime();

        // By default this will pick up application.yaml from the classpath
        Config config = Config.create();

        WebServer server = WebServer.builder(createRouting(config))
                                    .config(config.get("server"))
                                    .addMediaSupport(JsonpSupport.create())
                                    .addMediaSupport(JacksonSupport.create(JsonProvider.objectMapper()))
                                    .build();

        Single<WebServer> webserver = server.start();

        // Try to start the server. If successful, print some info and arrange to
        // print a message at shutdown. If unsuccessful, print the exception.
        webserver.thenAccept(ws -> {
                     System.out.println("WEB server is up! http://localhost:8080");
                     ws.whenShutdown().thenRun(() -> System.out.println("WEB server is DOWN. Good bye!"));
                 })
                 .exceptionallyAccept(t -> {
                     System.err.println("Startup failed: " + t.getMessage());
                     t.printStackTrace(System.err);
                 });

        return webserver;
    }

    /**
     * Creates new {@link Routing}.
     *
     * @param config configuration of this server
     * @return routing configured with JSON support, a health check, and a service
     */
    private static Routing createRouting(Config config) {

        MetricsSupport metrics = MetricsSupport.create();
        HealthSupport health = HealthSupport.builder()
                                            .addLiveness(HealthChecks.healthChecks())   // Adds a convenient set of checks
                                            .build();

        return Routing.builder()
                      .register(OpenAPISupport.create(config.get(OpenAPISupport.Builder.CONFIG_KEY)))
                      .register(health)                   // Health at "/health"
                      .register(metrics)                  // Metrics at "/metrics"
                      .register("/", new MessageServiceImpl())
                      .build();
    }
}
