/*
 * Copyright (c) 2019, 2022 Oracle and/or its affiliates.
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

package io.helidon.nima.tests.integration.server;

import io.helidon.common.http.ClientResponseHeaders;
import io.helidon.common.http.Http;
import io.helidon.common.http.Http.Header;
import io.helidon.common.http.Http.HeaderValues;
import io.helidon.common.testing.http.junit5.SocketHttpClient;
import io.helidon.nima.testing.junit5.webserver.ServerTest;
import io.helidon.nima.testing.junit5.webserver.SetUpRoute;
import io.helidon.nima.webserver.http.HttpRules;

import org.junit.jupiter.api.Test;

import static io.helidon.common.testing.http.junit5.HttpHeaderMatcher.hasHeader;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests transfer encoding.
 */
@ServerTest
class TransferEncodingTest {
    private static final Http.HeaderValue CONTENT_LENGTH_NINE = Header.create(Header.CONTENT_LENGTH, "9");
    private final SocketHttpClient socketHttpClient;

    TransferEncodingTest(SocketHttpClient socketHttpClient) {
        this.socketHttpClient = socketHttpClient;
    }

    @SetUpRoute
    static void routing(HttpRules rules) {
        rules.get("/length", (req, res) -> {
                    String payload = "It works!";
                    res.contentLength(payload.length());
                    res.send(payload);
                })
                .get("/chunked", (req, res) -> {
                    String payload = "It works!";
                    res.headers().add(HeaderValues.TRANSFER_ENCODING_CHUNKED);
                    res.send(payload);
                })
                .get("/optimized", (req, res) -> {
                    String payload = "It works!";
                    res.send(payload);
                })
                .get("/empty", (req, res) -> {
                    res.send();
                })
                .get("/emptychunked", (req, res) -> {
                    res.headers().add(HeaderValues.TRANSFER_ENCODING_CHUNKED);
                    res.send();
                });
    }

    /**
     * Test content length when no payload in response.
     */
    @Test
    void testEmptyContentLength() {
        String s = socketHttpClient.sendAndReceive("/empty", Http.Method.GET, null);

        ClientResponseHeaders headers = SocketHttpClient.headersFromResponse(s);
        assertThat(headers, hasHeader(HeaderValues.CONTENT_LENGTH_ZERO));
    }

    /**
     * Test when no payload in response but response was forced to chunked.
     */
    @Test
    void testEmptyChunked() {
        String s = socketHttpClient.sendAndReceive("/emptychunked", Http.Method.GET, null);
        ClientResponseHeaders headers = SocketHttpClient.headersFromResponse(s);
        assertThat(headers, hasHeader(HeaderValues.TRANSFER_ENCODING_CHUNKED));
    }

    /**
     * Test content length
     */
    @Test
    void testContentLength() {
        String s = socketHttpClient.sendAndReceive("/length", Http.Method.GET, null);
        assertThat(cutPayloadAndCheckHeadersFormat(s), is("It works!"));
        ClientResponseHeaders headers = SocketHttpClient.headersFromResponse(s);
        assertThat(headers, hasHeader(CONTENT_LENGTH_NINE));
    }

    /**
     * Test chunked encoding.
     */
    @Test
    void testChunkedEncoding() {
        String s = socketHttpClient.sendAndReceive("/chunked", Http.Method.GET, null);
        assertThat(cutPayloadAndCheckHeadersFormat(s), is("9\nIt works!\n0\n\n"));
        ClientResponseHeaders headers = SocketHttpClient.headersFromResponse(s);
        assertThat(headers, hasHeader(HeaderValues.TRANSFER_ENCODING_CHUNKED));
    }

    /**
     * Test optimized or content length in this case.
     */
    @Test
    void testOptimized() {
        String s = socketHttpClient.sendAndReceive("/optimized", Http.Method.GET, null);
        assertThat(cutPayloadAndCheckHeadersFormat(s), is("It works!"));
        ClientResponseHeaders headers = SocketHttpClient.headersFromResponse(s);
        assertThat(headers, hasHeader(CONTENT_LENGTH_NINE));
    }

    private String cutPayloadAndCheckHeadersFormat(String response) {
        assertThat(response, notNullValue());
        int index = response.indexOf("\n\n");
        if (index < 0) {
            throw new AssertionError("Missing end of headers in response!");
        }
        String headers = response.substring(0, index);
        String[] lines = headers.split("\\n");
        assertThat(lines[0], startsWith("HTTP/"));
        for (int i = 1; i < lines.length; i++) {
            assertThat(lines[i], containsString(":"));
        }
        return response.substring(index + 2);
    }
}
