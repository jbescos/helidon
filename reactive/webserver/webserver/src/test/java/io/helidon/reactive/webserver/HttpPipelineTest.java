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

package io.helidon.reactive.webserver;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import io.helidon.common.http.Http;
import io.helidon.common.testing.http.junit5.SocketHttpClient;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test support for HTTP 1.1 pipelining.
 */
@Disabled("Fails on build pipelines, will change once on Nima")
public class HttpPipelineTest {
    private static final Logger LOGGER = Logger.getLogger(HttpPipelineTest.class.getName());
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    private static final AtomicInteger counter = new AtomicInteger(0);
    private static final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1);

    private static WebServer webServer;
    private static SocketHttpClient client;

    @BeforeAll
    public static void startServer() throws Exception {
        startServer(0);
    }

    @AfterAll
    public static void close() throws Exception {
        if (webServer != null) {
            webServer.shutdown()
                    .await(TIMEOUT);
        }
    }

    /**
     * Pipelines request_0 and request_1 and makes sure responses are returned in the
     * correct order. Note that the server will delay the response for request_0 to
     * make sure they are properly synchronized.
     *
     * @throws Exception If there are connection problems.
     */
    @Test
    public void testPipelining() throws Exception {
        client.request(Http.Method.PUT, "/");        // reset server
        client.request(Http.Method.GET, null);        // request_0
        client.request(Http.Method.GET, null);        // request_1
        log("put client");
        String reset = client.receive();
        assertThat(reset, notNullValue());
        log("request0 client");
        String request_0 = client.receive();
        assertThat(request_0, containsString("Response 0"));
        log("request1 client");
        String request_1 = client.receive();
        assertThat(request_1, containsString("Response 1"));
    }

    private static void startServer(int port) {
        webServer = WebServer.builder()
                .host("localhost")
                .port(port)
                .routing(r -> r
                        .put("/", (req, res) -> {
                            counter.set(0);
                            log("put server");
                            req.content()
                                    .as(String.class)
                                    .forSingle(it -> {
                                        log("put: " + it);
                                        res.send();
                                    });
                        })
                        .get("/", (req, res) -> {
                            log("get server");
                            int n = counter.getAndIncrement();
                            int delay = (n % 2 == 0) ? 1000 : 0;    // alternate delay 1 second and no delay
                            executor.schedule(() -> {
                                                  log("get server schedule");
                                                  res.status(Http.Status.OK_200).send("Response " + n + "\n");
                                              },
                                              delay, TimeUnit.MILLISECONDS);
                        })
                )
                .build()
                .start()
                .await(TIMEOUT);

        client = SocketHttpClient.create(webServer.port());

        LOGGER.info("Started server at: https://localhost:" + webServer.port());
    }

    private static void log(String prefix) {
        LOGGER.info(() -> prefix + " " + Thread.currentThread());
    }
}
