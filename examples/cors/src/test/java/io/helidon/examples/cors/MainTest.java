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

package io.helidon.examples.cors;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import io.helidon.common.http.Headers;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.config.Config;
import io.helidon.cors.CrossOriginConfig;
import io.helidon.reactive.media.jsonp.JsonpSupport;
import io.helidon.reactive.webclient.WebClient;
import io.helidon.reactive.webclient.WebClientRequestBuilder;
import io.helidon.reactive.webclient.WebClientRequestHeaders;
import io.helidon.reactive.webclient.WebClientResponse;
import io.helidon.reactive.webclient.WebClientResponseHeaders;
import io.helidon.reactive.webserver.WebServer;

import jakarta.json.JsonObject;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.helidon.common.http.Http.Header.ACCESS_CONTROL_ALLOW_METHODS;
import static io.helidon.common.http.Http.Header.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.helidon.common.http.Http.Header.ACCESS_CONTROL_REQUEST_METHOD;
import static io.helidon.common.http.Http.Header.HOST;
import static io.helidon.common.http.Http.Header.ORIGIN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MainTest {

    private static WebServer webServer;
    private static WebClient webClient;

    @BeforeAll
    public static void start() throws Exception {
        // the port is only available if the server started already!
        // so we need to wait
        webServer = Main.startServer().await();

        webClient = WebClient.builder()
                        .baseUri("http://localhost:" + webServer.port())
                        .addMediaSupport(JsonpSupport.create())
                        .build();

        long timeout = 2000; // 2 seconds should be enough to start the server
        long now = System.currentTimeMillis();

        while (!webServer.isRunning()) {
            Thread.sleep(100);
            if ((System.currentTimeMillis() - now) > timeout) {
                Assertions.fail("Failed to start webserver");
            }
        }
    }

    @AfterAll
    public static void stop() {
        if (webServer != null) {
            webServer.shutdown()
                     .await(10, TimeUnit.SECONDS);
        }
    }

    @Order(1) // Make sure this runs before the greeting message changes so responses are deterministic.
    @Test
    public void testHelloWorld() {

        WebClientResponse r = getResponse("/greet");

        assertEquals(200, r.status().code(), "HTTP response1");
        assertEquals("Hello World!", fromPayload(r).getMessage(),
                "default message");

        r = getResponse("/greet/Joe");
        assertEquals(200, r.status().code(), "HTTP response2");
        assertEquals("Hello Joe!", fromPayload(r).getMessage(),
                "hello Joe message");

        r = putResponse("/greet/greeting", new GreetingMessage("Hola"));
        assertEquals(204, r.status().code(), "HTTP response3");

        r = getResponse("/greet/Jose");
        assertEquals(200, r.status().code(), "HTTP response4");
        assertEquals("Hola Jose!", fromPayload(r).getMessage(),
                "hola Jose message");

        r = getResponse("/health");
        assertEquals(200, r.status().code(), "HTTP response2");

        r = getResponse("/metrics");
        assertEquals(200, r.status().code(), "HTTP response2");
    }

    @Order(10) // Run after the non-CORS tests (so the greeting is Hola) but before the CORS test that changes the greeting again.
    @Test
    void testAnonymousGreetWithCors() {
        WebClientRequestBuilder builder = webClient.get();
        WebClientRequestHeaders headers = builder.headers();
        headers.set(ORIGIN, "http://foo.com");
        headers.set(HOST, "here.com");

        WebClientResponse r = getResponse("/greet", builder);
        assertEquals(200, r.status().code(), "HTTP response");
        String payload = fromPayload(r).getMessage();
        assertTrue(payload.contains("Hola World"), "HTTP response payload was " + payload);
        Headers responseHeaders = r.headers();
        Optional<String> allowOrigin = responseHeaders.value(ACCESS_CONTROL_ALLOW_ORIGIN);
        assertTrue(allowOrigin.isPresent(),
                "Expected CORS header " + CrossOriginConfig.ACCESS_CONTROL_ALLOW_ORIGIN + " is absent");
        assertEquals(allowOrigin.get(), "*");
    }

    @Order(11) // Run after the non-CORS tests but before other CORS tests.
    @Test
    void testGreetingChangeWithCors() {

        // Send the pre-flight request and check the response.

        WebClientRequestBuilder builder = webClient.options();
        WebClientRequestHeaders headers = builder.headers();
        headers.set(ORIGIN, "http://foo.com");
        headers.set(HOST, "here.com");
        headers.set(ACCESS_CONTROL_REQUEST_METHOD, "PUT");

        WebClientResponse r = builder.path("/greet/greeting")
                .submit()
                .await();

        Headers responseHeaders = r.headers();
        List<String> allowMethods = responseHeaders.values(ACCESS_CONTROL_ALLOW_METHODS);
        assertFalse(allowMethods.isEmpty(),
                "pre-flight response does not include " + CrossOriginConfig.ACCESS_CONTROL_ALLOW_METHODS);
        assertTrue(allowMethods.contains("PUT"));
        List<String> allowOrigins = responseHeaders.values(ACCESS_CONTROL_ALLOW_ORIGIN);
        assertFalse(allowOrigins.isEmpty(),
                "pre-flight response does not include " + CrossOriginConfig.ACCESS_CONTROL_ALLOW_ORIGIN);
        assertTrue(allowOrigins.contains("http://foo.com"), "Header " + CrossOriginConfig.ACCESS_CONTROL_ALLOW_ORIGIN
                + " should contain '*' but does not; " + allowOrigins);

        // Send the follow-up request.

        builder = webClient.put();
        headers = builder.headers();
        headers.set(ORIGIN, "http://foo.com");
        headers.set(HOST, "here.com");
        headers.addAll(responseHeaders);

        r = putResponse("/greet/greeting", new GreetingMessage("Cheers"), builder);
        assertEquals(204, r.status().code(), "HTTP response3");
        responseHeaders = r.headers();
        allowOrigins = responseHeaders.values(ACCESS_CONTROL_ALLOW_ORIGIN);
        assertFalse(allowOrigins.isEmpty(),
                "Expected CORS header " + CrossOriginConfig.ACCESS_CONTROL_ALLOW_ORIGIN + " has no value(s)");
        assertTrue(allowOrigins.contains("http://foo.com"), "Header " + CrossOriginConfig.ACCESS_CONTROL_ALLOW_ORIGIN
                + " should contain '*' but does not; " + allowOrigins);
    }

    @Order(12) // Run after CORS test changes greeting to Cheers.
    @Test
    void testNamedGreetWithCors() {
        WebClientRequestBuilder builder = webClient.get();
        WebClientRequestHeaders headers = builder.headers();
        headers.set(ORIGIN, "http://foo.com");
        headers.set(HOST, "here.com");

        WebClientResponse r = getResponse("/greet/Maria", builder);
        assertEquals(200, r.status().code(), "HTTP response");
        assertTrue(fromPayload(r).getMessage().contains("Cheers Maria"));
        WebClientResponseHeaders responseHeaders = r.headers();
        Optional<String> allowOrigin = responseHeaders.value(ACCESS_CONTROL_ALLOW_ORIGIN);
        assertTrue(allowOrigin.isPresent(),
                "Expected CORS header " + CrossOriginConfig.ACCESS_CONTROL_ALLOW_ORIGIN + " is absent");
        assertEquals(allowOrigin.get(), "*");
    }

    @Order(100) // After all other tests so we can rely on deterministic greetings.
    @Test
    void testGreetingChangeWithCorsAndOtherOrigin() {
        WebClientRequestBuilder builder = webClient.put();
        WebClientRequestHeaders headers = builder.headers();
        headers.set(ORIGIN, "http://other.com");
        headers.set(HOST, "here.com");

        WebClientResponse r = putResponse("/greet/greeting", new GreetingMessage("Ahoy"), builder);
        // Result depends on whether we are using overrides or not.
        boolean isOverriding = Config.create().get("cors").exists();
        assertEquals(isOverriding ? 204 : 403, r.status().code(), "HTTP response3");
    }

    private static WebClientResponse getResponse(String path) {
        return getResponse(path, webClient.get());
    }

    private static WebClientResponse getResponse(String path, WebClientRequestBuilder builder) {
        return builder
                .accept(MediaTypes.APPLICATION_JSON)
                .path(path)
                .submit()
                .await();
    }

    private static WebClientResponse putResponse(String path, GreetingMessage payload) {
        return putResponse(path, payload, webClient.put());
    }

    private static WebClientResponse putResponse(String path, GreetingMessage payload, WebClientRequestBuilder builder) {
        return builder
                .accept(MediaTypes.APPLICATION_JSON)
                .path(path)
                .submit(payload.forRest())
                .await();
    }

    private static GreetingMessage fromPayload(WebClientResponse response) {
        JsonObject json = response
                .content()
                .as(JsonObject.class)
                .await();

        return GreetingMessage.fromRest(json);
    }
}
