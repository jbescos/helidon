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

package io.helidon.examples.webserver.multiport;

import io.helidon.common.reactive.Single;
import io.helidon.config.Config;
import io.helidon.health.checks.HealthChecks;
import io.helidon.logging.common.LogConfig;
import io.helidon.reactive.health.HealthSupport;
import io.helidon.reactive.metrics.MetricsSupport;
import io.helidon.reactive.webserver.Routing;
import io.helidon.reactive.webserver.WebServer;

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
     * @param args command line arguments.
     */
    public static void main(final String[] args) {
        // By default this will pick up application.yaml from the classpath
        Config config = Config.create();
        startServer(config);
    }

    /**
     * Start the server.
     * @return the created {@link WebServer} instance
     */
    static Single<WebServer> startServer(Config config) {
        // load logging configuration
        LogConfig.configureRuntime();

        // Build server using three ports:
        // default public port, admin port, private port
        WebServer server = WebServer.builder(createPublicRouting())
                .config(config.get("server"))
                // Add a set of routes on the named socket "admin"
                .addNamedRouting("admin", createAdminRouting())
                // Add a set of routes on the named socket "private"
                .addNamedRouting("private", createPrivateRouting())
                .build();

        Single<WebServer> webserver = server.start();

        // Try to start the server. If successful, print some info and arrange to
        // print a message at shutdown. If unsuccessful, print the exception.
        webserver.thenAccept(ws -> {
                    System.out.println(
                            "WEB server is up! http://localhost:" + ws.port());
                    ws.whenShutdown().thenRun(()
                            -> System.out.println("WEB server is DOWN. Good bye!"));
                })
                .exceptionally(t -> {
                    System.err.println("Startup failed: " + t.getMessage());
                    t.printStackTrace(System.err);
                    return null;
                });

        // Server threads are not daemon. No need to block. Just react.

        return webserver;
    }

    /**
     * Creates private {@link Routing}.
     *
     * @return routing for use on "private" port
     */
    private static Routing createPrivateRouting() {
        return Routing.builder()
                .get("/private/hello", (req, res) -> res.send("Private Hello!!"))
                .build();
    }

    /**
     * Creates public {@link Routing}.
     *
     * @return routing for use on "public" port
     */
    private static Routing createPublicRouting() {
        return Routing.builder()
                .get("/hello", (req, res) -> res.send("Public Hello!!"))
                .build();
    }

    /**
     * Creates admin {@link Routing}.
     *
     * @return routing for use on admin port
     */
    private static Routing createAdminRouting() {
        MetricsSupport metrics = MetricsSupport.create();
        HealthSupport health = HealthSupport.builder()
                .add(HealthChecks.healthChecks())   // Adds a convenient set of checks
                .build();

        return Routing.builder()
                .register(health)
                .register(metrics)
                .build();
    }
}
