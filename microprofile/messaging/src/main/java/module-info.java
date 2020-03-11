/*
 * Copyright (c) 2018, 2020 Oracle and/or its affiliates.
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
 * MicroProfile Reactive Messaging implementation.
 */
module io.helidon.microprofile.messaging {
    requires java.logging;

    requires static cdi.api;
    requires static javax.inject;
    requires static java.activation;
    requires javax.interceptor.api;
    requires io.helidon.config;
    requires org.reactivestreams;
    requires transitive microprofile.reactive.messaging.api;
    requires transitive microprofile.reactive.streams.operators.api;

    exports io.helidon.microprofile.messaging;

    provides javax.enterprise.inject.spi.Extension with io.helidon.microprofile.messaging.MessagingCdiExtension;
}
