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

package io.helidon.pico.builder.config.fakes;

import java.util.Objects;
import java.util.Optional;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

//@ConfiguredBy(FakeServerConfig.class)
public class FakeWebServer implements WebServer {

    private FakeServerConfig cfg;
    private boolean running;

    @Inject
    FakeWebServer(FakeServerConfig cfg, Optional<FakeTracer> tracer) {
        this.cfg = Objects.requireNonNull(cfg);
    }

//    /**
//     * The traditional approach.
//     */
//    FakeWebServer(WebServer.Builder builder) {
//    }

    @PostConstruct
    public void initialize() {
        running = true;
    }

    @Override
    public FakeServerConfig configuration() {
        return cfg;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
