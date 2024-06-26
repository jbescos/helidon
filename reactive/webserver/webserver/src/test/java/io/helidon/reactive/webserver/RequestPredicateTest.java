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
package io.helidon.reactive.webserver;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import io.helidon.common.http.Http;
import io.helidon.common.http.HttpMediaType;
import io.helidon.reactive.webserver.RoutingTest.RoutingChecker;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.helidon.common.http.Http.Header.ACCEPT;
import static io.helidon.common.http.Http.Header.CONTENT_TYPE;
import static io.helidon.common.http.Http.Header.COOKIE;
import static io.helidon.reactive.webserver.RoutingTest.mockResponse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests {@link RequestPredicate}.
 */
public class RequestPredicateTest {

    public static final Http.HeaderName MY_HEADER = Http.Header.create("my-header");

    @Test
    public void isOfMethod1() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .any("/getOrPost", RequestPredicate.create()
                        .isOfMethod("GET", "POST")
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("methodFound");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("methodNotFound");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .isOfMethod((String[]) null);
        });

        routing.route(mockRequest("/getOrPost"), mockResponse());
        assertThat(checker.handlersInvoked(), is("methodFound"));

        checker.reset();
        routing.route(mockRequest("/getOrPost", Http.Method.PUT), mockResponse());
        assertThat(checker.handlersInvoked(), is("methodNotFound"));
    }

    @Test
    public void isOfMethod2() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .any("/getOrPost", RequestPredicate.create()
                        .isOfMethod(Http.Method.GET, Http.Method.POST)
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("methodFound");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("methodNotFound");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .isOfMethod((Http.Method[]) null);
        });

        routing.route(mockRequest("/getOrPost"), mockResponse());
        assertThat(checker.handlersInvoked(), is("methodFound"));

        checker.reset();
        routing.route(mockRequest("/getOrPost", Http.Method.PUT), mockResponse());
        assertThat(checker.handlersInvoked(), is("methodNotFound"));
    }

    @Test
    public void containsHeader() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/exists", RequestPredicate.create()
                        .containsHeader(MY_HEADER)
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("headerFound");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("headerNotFound");
                        }))
                .get("/valid", RequestPredicate.create()
                        .containsHeader(MY_HEADER, "abc"::equals)
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("headerIsValid");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("headerIsNotValid");
                        }))
                .get("/equals", RequestPredicate.create()
                        .containsHeader(MY_HEADER, "abc")
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("headerIsEqual");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("headerIsNotEqual");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsHeader(null);
        });

        routing.route(mockRequest("/exists", Map.of(MY_HEADER, List.of("abc"))),
                mockResponse());
        assertThat(checker.handlersInvoked(), is("headerFound"));

        checker.reset();
        routing.route(mockRequest("/exists", Map.of()), mockResponse());
        assertThat(checker.handlersInvoked(), is("headerNotFound"));
    }

    @Test
    public void containsValidHeader() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/valid", RequestPredicate.create()
                        .containsHeader(MY_HEADER, "abc"::equals)
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("headerIsValid");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("headerIsNotValid");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsHeader(MY_HEADER, (Predicate<String>) null);
        });

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsHeader(null, "abc"::equals);
        });

        routing.route(mockRequest("/valid", Map.of(MY_HEADER, List.of("abc"))),
                mockResponse());
        assertThat(checker.handlersInvoked(), is("headerIsValid"));

        checker.reset();
        routing.route(mockRequest("/valid", Map.of(MY_HEADER, List.of("def"))),
                mockResponse());
        assertThat(checker.handlersInvoked(), is("headerIsNotValid"));
    }

    @Test
    public void containsExactHeader() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/equals", RequestPredicate.create()
                        .containsHeader(MY_HEADER, "abc")
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("headerIsEqual");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("headerIsNotEqual");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsHeader(MY_HEADER, (String) null);
        });

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsHeader(null, "abc");
        });

        routing.route(mockRequest("/equals", Map.of(MY_HEADER, List.of("abc"))),
                mockResponse());
        assertThat(checker.handlersInvoked(), is("headerIsEqual"));

        checker.reset();
        routing.route(mockRequest("/equals", Map.of(MY_HEADER, List.of("def"))),
                mockResponse());
        assertThat(checker.handlersInvoked(), is("headerIsNotEqual"));
    }

    @Test
    public void containsQueryParam() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/exists", RequestPredicate.create()
                        .containsQueryParameter("my-param")
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("queryParamFound");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("queryParamNotFound");
                        }))
                .get("/valid", RequestPredicate.create()
                        .containsQueryParameter("my-param", "abc"::equals)
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("queryParamIsValid");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("queryParamIsNotValid");
                        }))
                .get("/equals", RequestPredicate.create()
                        .containsQueryParameter("my-param", "abc")
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("queryParamIsEqual");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("queryParamIsNotEqual");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsQueryParameter(null);
        });

        routing.route(mockRequest("/exists?my-param=abc"), mockResponse());
        assertThat(checker.handlersInvoked(), is("queryParamFound"));

        checker.reset();
        routing.route(mockRequest("/exists"), mockResponse());
        assertThat(checker.handlersInvoked(), is("queryParamNotFound"));
    }

    @Test
    public void containsValidQueryParam() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/valid", RequestPredicate.create()
                        .containsQueryParameter("my-param", "abc"::equals)
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("queryParamIsValid");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("queryParamIsNotValid");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsQueryParameter("my-param", (Predicate<String>) null);
        });

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsQueryParameter(null, "abc");
        });

        routing.route(mockRequest("/valid?my-param=abc"), mockResponse());
        assertThat(checker.handlersInvoked(), is("queryParamIsValid"));

        checker.reset();
        routing.route(mockRequest("/valid?my-param=def"), mockResponse());
        assertThat(checker.handlersInvoked(), is("queryParamIsNotValid"));
    }

    @Test
    public void containsExactQueryParam() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/equals", RequestPredicate.create()
                        .containsQueryParameter("my-param", "abc")
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("queryParamIsEqual");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("queryParamIsNotEqual");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsQueryParameter("my-param", (String) null);
        });

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsQueryParameter(null, "abc");
        });

        routing.route(mockRequest("/equals?my-param=abc"), mockResponse());
        assertThat(checker.handlersInvoked(), is("queryParamIsEqual"));

        checker.reset();
        routing.route(mockRequest("/equals?my-param=def"), mockResponse());
        assertThat(checker.handlersInvoked(), is("queryParamIsNotEqual"));
    }

    @Test
    public void containsCookie() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/exists", RequestPredicate.create()
                        .containsCookie("my-cookie")
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("cookieFound");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("cookieNotFound");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsCookie(null);
        });

        routing.route(mockRequest("/exists", Map.of(COOKIE,
                                                    List.of("my-cookie=abc"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("cookieFound"));

        checker.reset();
        routing.route(mockRequest("/exists", Map.of(COOKIE,
                List.of("other-cookie=abc"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("cookieNotFound"));
    }

    @Test
    public void containsValidCookie() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/valid", RequestPredicate.create()
                        .containsCookie("my-cookie", "abc"::equals)
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("cookieIsValid");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("cookieIsNotValid");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsCookie("my-cookie", (Predicate<String>) null);
        });

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsCookie(null, "abc");
        });

        routing.route(mockRequest("/valid", Map.of(COOKIE,
                List.of("my-cookie=abc"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("cookieIsValid"));

        checker.reset();
        routing.route(mockRequest("/valid", Map.of(COOKIE,
                List.of("my-cookie=def"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("cookieIsNotValid"));

        checker.reset();
        routing.route(mockRequest("/valid", Map.of(COOKIE,
                List.of("my-cookie="))), mockResponse());
        assertThat(checker.handlersInvoked(), is("cookieIsNotValid"));
    }

    @Test
    public void containsExactCookie() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/equals", RequestPredicate.create()
                        .containsCookie("my-cookie", "abc")
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("cookieIsEqual");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("cookieIsNotEqual");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsCookie("my-cookie", (String) null);
        });

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .containsCookie(null, "abc");
        });

        routing.route(mockRequest("/equals", Map.of(COOKIE,
                List.of("my-cookie=abc"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("cookieIsEqual"));

        checker.reset();
        routing.route(mockRequest("/equals", Map.of(COOKIE,
                List.of("my-cookie=def"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("cookieIsNotEqual"));
    }

    @Test
    public void accepts1() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/accepts1", RequestPredicate.create()
                        .accepts("text/plain", "application/json")
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("acceptsMediaType");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("doesNotAcceptMediaType");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .accepts((String[]) null);
        });

        routing.route(mockRequest("/accepts1", Map.of(ACCEPT,
                List.of("application/json"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("acceptsMediaType"));

        checker.reset();
        routing.route(mockRequest("/accepts1", Map.of(ACCEPT,
                List.of("text/plain"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("acceptsMediaType"));

        checker.reset();
        routing.route(mockRequest("/accepts1", Map.of(ACCEPT,
                List.of("text/plain", "application/xml"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("acceptsMediaType"));

        checker.reset();
        routing.route(mockRequest("/accepts1", Map.of(ACCEPT, List.of())),
                mockResponse());
        assertThat(checker.handlersInvoked(), is("acceptsMediaType"));

        checker.reset();
        routing.route(mockRequest("/accepts1", Map.of()), mockResponse());
        assertThat(checker.handlersInvoked(), is("acceptsMediaType"));

        checker.reset();
        routing.route(mockRequest("/accepts1", Map.of(ACCEPT,
                List.of("application/xml"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("doesNotAcceptMediaType"));
    }

    @Test
    public void accepts2() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/accepts2", RequestPredicate.create()
                        .accepts(HttpMediaType.PLAINTEXT_UTF_8,
                                HttpMediaType.JSON_UTF_8)
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("acceptsMediaType");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("doesNotAcceptMediaType");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .accepts((HttpMediaType[]) null);
        });

        routing.route(mockRequest("/accepts2", Map.of(ACCEPT,
                List.of("application/json"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("acceptsMediaType"));

        checker.reset();
        routing.route(mockRequest("/accepts2", Map.of(ACCEPT,
                List.of("text/plain"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("acceptsMediaType"));

        checker.reset();
        routing.route(mockRequest("/accepts2", Map.of(ACCEPT,
                List.of("text/plain", "application/xml"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("acceptsMediaType"));

        checker.reset();
        routing.route(mockRequest("/accepts2", Map.of(ACCEPT, List.of())),
                mockResponse());
        assertThat(checker.handlersInvoked(), is("acceptsMediaType"));

        checker.reset();
        routing.route(mockRequest("/accepts2", Map.of()), mockResponse());
        assertThat(checker.handlersInvoked(), is("acceptsMediaType"));

        checker.reset();
        routing.route(mockRequest("/accepts2", Map.of(ACCEPT,
                List.of("application/xml"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("doesNotAcceptMediaType"));
    }

    @Test
    public void hasContentType1() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/contentType1", RequestPredicate.create()
                        .hasContentType("text/plain", "application/json")
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("hasContentType");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("doesNotHaveContentType");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .hasContentType((String[]) null);
        });

        routing.route(mockRequest("/contentType1", Map.of(CONTENT_TYPE,
                List.of("text/plain"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("hasContentType"));

        checker.reset();
        routing.route(mockRequest("/contentType1", Map.of(CONTENT_TYPE,
                List.of("text/plain"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("hasContentType"));

        checker.reset();
        routing.route(mockRequest("/contentType1", Map.of(CONTENT_TYPE,
                List.of("application/json"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("hasContentType"));

        checker.reset();
        routing.route(mockRequest("/contentType1", Map.of(CONTENT_TYPE,
                List.of("application/xml"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("doesNotHaveContentType"));

        checker.reset();
        routing.route(mockRequest("/contentType1", Map.of(CONTENT_TYPE,
                List.of())), mockResponse());
        assertThat(checker.handlersInvoked(), is("doesNotHaveContentType"));

        checker.reset();
        routing.route(mockRequest("/contentType1", Map.of()), mockResponse());
        assertThat(checker.handlersInvoked(), is("doesNotHaveContentType"));
    }

    @Test
    public void hasContentType2() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/contentType2", RequestPredicate.create()
                        .hasContentType(HttpMediaType.TEXT_PLAIN,
                                HttpMediaType.APPLICATION_JSON)
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("hasContentType");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("doesNotHaveContentType");
                        }))
                .build();

        assertThrows(NullPointerException.class, () -> {
            RequestPredicate.create()
                    .hasContentType((HttpMediaType[]) null);
        });

        routing.route(mockRequest("/contentType2", Map.of(CONTENT_TYPE,
                List.of("text/plain"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("hasContentType"));

        checker.reset();
        routing.route(mockRequest("/contentType2", Map.of(CONTENT_TYPE,
                List.of("text/plain"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("hasContentType"));

        checker.reset();
        routing.route(mockRequest("/contentType2", Map.of(CONTENT_TYPE,
                List.of("application/json"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("hasContentType"));

        checker.reset();
        routing.route(mockRequest("/contentType2", Map.of(CONTENT_TYPE,
                List.of("application/xml"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("doesNotHaveContentType"));

        checker.reset();
        routing.route(mockRequest("/contentType2", Map.of(CONTENT_TYPE,
                List.of())), mockResponse());
        assertThat(checker.handlersInvoked(), is("doesNotHaveContentType"));

        checker.reset();
        routing.route(mockRequest("/contentType2", Map.of()), mockResponse());
        assertThat(checker.handlersInvoked(), is("doesNotHaveContentType"));
    }

    @Test
    public void multipleConditions() {
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .any("/multiple", RequestPredicate.create()
                        .accepts(HttpMediaType.TEXT_PLAIN)
                        .hasContentType(HttpMediaType.TEXT_PLAIN)
                        .containsQueryParameter("my-param")
                        .containsCookie("my-cookie")
                        .isOfMethod(Http.Method.GET)
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("hasAllConditions");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("doesNotHaveAllConditions");
                        }))
                .build();

        routing.route(mockRequest("/multiple?my-param=abc",
                Map.of(CONTENT_TYPE, List.of("text/plain"),
                      ACCEPT, List.of("text/plain"),
                      COOKIE, List.of("my-cookie=abc"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("hasAllConditions"));

        checker.reset();
        routing.route(mockRequest("/multiple?my-param=abc",
                Map.of(ACCEPT, List.of("text/plain"),
                      COOKIE, List.of("my-cookie=abc"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("doesNotHaveAllConditions"));
    }

    @Test
    public void and(){
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/and", RequestPredicate.create()
                        .accepts(HttpMediaType.PLAINTEXT_UTF_8)
                        .and((req) -> req.headers().contains(MY_HEADER))
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("hasAllConditions");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("doesNotHaveAllConditions");
                        }))
                .build();

        routing.route(mockRequest("/and",
                Map.of(ACCEPT, List.of("text/plain"),
                      MY_HEADER, List.of("abc"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("hasAllConditions"));

        checker.reset();
        routing.route(mockRequest("/and",
                Map.of(ACCEPT, List.of("text/plain"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("doesNotHaveAllConditions"));
    }

    @Test
    public void or(){
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/or", RequestPredicate.create()
                        .hasContentType(HttpMediaType.TEXT_PLAIN)
                        .or((req) -> req.headers().contains(MY_HEADER))
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("hasAnyCondition");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("doesNotHaveAnyCondition");
                        }))
                .build();

        routing.route(mockRequest("/or",
                Map.of(CONTENT_TYPE, List.of("text/plain"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("hasAnyCondition"));

        checker.reset();
        routing.route(mockRequest("/or",
                Map.of(MY_HEADER, List.of("abc"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("hasAnyCondition"));

        checker.reset();
        routing.route(mockRequest("/or", Map.of()), mockResponse());
        assertThat(checker.handlersInvoked(), is("doesNotHaveAnyCondition"));
    }

    @Test
    public void negate(){
        final RoutingChecker checker = new RoutingChecker();
        Routing routing = Routing.builder()
                .get("/negate", RequestPredicate.create()
                        .hasContentType(HttpMediaType.TEXT_PLAIN)
                        .containsCookie("my-cookie")
                        .negate()
                        .thenApply((req, resp) -> {
                            checker.handlerInvoked("hasAllConditions");
                        }).otherwise((req, res) -> {
                            checker.handlerInvoked("doesNotHaveAllConditions");
                        }))
                .build();

        routing.route(mockRequest("/negate",
                Map.of(CONTENT_TYPE, List.of("application/json"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("hasAllConditions"));

        checker.reset();
        routing.route(mockRequest("/negate",
                Map.of(CONTENT_TYPE, List.of("text/plain"),
                        COOKIE, List.of("my-cookie=abc"))), mockResponse());
        assertThat(checker.handlersInvoked(), is("doesNotHaveAllConditions"));
    }

    @Test
    public void nextAlreadySet(){
        RequestPredicate requestPredicate = RequestPredicate.create()
                .containsCookie("my-cookie");
        requestPredicate.containsHeader(MY_HEADER);
        assertThrows(IllegalStateException.class, () -> {
            requestPredicate.containsHeader(Http.Header.create("my-param"));
        });
    }

    private static BareRequest mockRequest(final String path) {
        BareRequest bareRequestMock = RoutingTest.mockRequest(path,
                Http.Method.GET);
        return bareRequestMock;
    }

    private static BareRequest mockRequest(final String path,
            final Http.Method method) {

        BareRequest bareRequestMock = RoutingTest.mockRequest(path, method);
        return bareRequestMock;
    }

    private static BareRequest mockRequest(final String path,
            final Map<Http.HeaderName, List<String>> headers) {

        BareRequest bareRequestMock = RoutingTest.mockRequest(path, Http.Method.GET);
        HttpHeaders nettyHeaders = new DefaultHttpHeaders();
        headers.forEach((key, value) -> nettyHeaders.set(key.defaultCase(), value));
        RequestHeaders rh = new NettyRequestHeaders(nettyHeaders);
        Mockito.doReturn(rh).when(bareRequestMock).headers();
        return bareRequestMock;
    }
}
