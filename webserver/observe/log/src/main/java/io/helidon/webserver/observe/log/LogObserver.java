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

package io.helidon.webserver.observe.log;

import java.util.function.Consumer;

import io.helidon.builder.api.RuntimeType;
import io.helidon.webserver.http.HttpRouting;
import io.helidon.webserver.observe.spi.Observer;

/**
 * Observer for information about loggers, and possibly to obtain log stream.
 */
@RuntimeType.PrototypedBy(LogObserverConfig.class)
public class LogObserver implements Observer, RuntimeType.Api<LogObserverConfig> {
    private final LogObserverConfig config;

    private LogObserver(LogObserverConfig config) {
        this.config = config;
    }

    /**
     * Create a new builder to configure Log observer.
     *
     * @return a new builder
     */
    public static LogObserverConfig.Builder builder() {
        return LogObserverConfig.builder();
    }

    /**
     * Create a new Log observer using the provided configuration.
     *
     * @param config configuration
     * @return a new observer
     */
    public static LogObserver create(LogObserverConfig config) {
        return new LogObserver(config);
    }

    /**
     * Create a new Log observer customizing its configuration.
     *
     * @param consumer configuration consumer
     * @return a new observer
     */
    public static LogObserver create(Consumer<LogObserverConfig.Builder> consumer) {
        return builder()
                .update(consumer)
                .build();
    }

    /**
     * Create a new Log observer with default configuration.
     *
     * @return a new observer
     */
    public static LogObserver create() {
        return builder()
                .build();
    }

    @Override
    public LogObserverConfig prototype() {
        return config;
    }

    @Override
    public String type() {
        return "log";
    }

    @Override
    public void register(HttpRouting.Builder routing, String endpoint) {
        // register the service itself
        routing.register(endpoint, new LogService(this.config));
    }
}
