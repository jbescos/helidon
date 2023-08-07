/*
 * Copyright (c) 2023 Oracle and/or its affiliates.
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

/**
 * Unit and integration testing support for Níma WebSocket and JUnit 5.
 */
module io.helidon.nima.testing.junit5.http2 {
    requires transitive io.helidon.nima.testing.junit5.webserver;
    requires io.helidon.nima.http2.webserver;
    requires io.helidon.nima.http2.webclient;

    exports io.helidon.nima.testing.junit5.http2;

    provides io.helidon.nima.testing.junit5.webserver.spi.ServerJunitExtension
            with io.helidon.nima.testing.junit5.http2.Http2ServerExtension;
}