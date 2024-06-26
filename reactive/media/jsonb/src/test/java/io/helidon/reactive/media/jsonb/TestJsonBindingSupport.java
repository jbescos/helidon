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
package io.helidon.reactive.media.jsonb;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.helidon.common.GenericType;
import io.helidon.common.http.Http;
import io.helidon.common.http.HttpMediaType;
import io.helidon.reactive.webserver.Handler;
import io.helidon.reactive.webserver.Routing;
import io.helidon.reactive.webserver.testsupport.MediaPublisher;
import io.helidon.reactive.webserver.testsupport.TestClient;
import io.helidon.reactive.webserver.testsupport.TestResponse;

import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests {@link JsonbSupport}.
 */
public class TestJsonBindingSupport {

    @Test
    public void pingPong() throws Exception {
        final Routing routing = Routing.builder()
            .post("/foo", Handler.create(Person.class, (req, res, person) -> res.send(person)))
            .build();
        final String personJson = "{\"name\":\"Frank\"}";
        final TestResponse response = TestClient.create(routing, JsonbSupport.create())
            .path("/foo")
            .post(MediaPublisher.create(HttpMediaType.JSON_UTF_8, personJson));

        assertThat(response.headers().first(Http.Header.CONTENT_TYPE).orElse(null),
                is(HttpMediaType.APPLICATION_JSON.text()));
        final String json = response.asString().get(10, TimeUnit.SECONDS);
        assertThat(json, is(personJson));
    }

    @Test
    public void genericType() throws Exception {
        GenericType<List<Person>> personsType = new GenericType<>() { };
        final Routing routing = Routing.builder()
                .post("/foo", (req, res) -> {
                    req.content().as(personsType)
                            .thenAccept(res::send);
                })
                .build();

        final String personsJson = "[{\"name\":\"Frank\"},{\"name\":\"John\"}]";
        final TestResponse response = TestClient.create(routing, JsonbSupport.create())
            .path("/foo")
            .post(MediaPublisher.create(HttpMediaType.JSON_UTF_8, personsJson));
        assertThat(response.headers().first(Http.Header.CONTENT_TYPE).orElse(null),
                is(HttpMediaType.APPLICATION_JSON.text()));
        final String json = response.asString().get(10, TimeUnit.SECONDS);
        assertThat(json, is(personsJson));
    }

    public static final class Person {

        private String name;

        public Person() {
            super();
        }

        public String getName() {
            return this.name;
        }

        public void setName(final String name) {
            this.name = name;
        }

    }
}
