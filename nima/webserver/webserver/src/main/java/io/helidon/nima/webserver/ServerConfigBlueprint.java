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

package io.helidon.nima.webserver;

import java.util.Map;
import java.util.Optional;

import io.helidon.builder.api.Prototype;
import io.helidon.common.context.Context;
import io.helidon.config.ConfigException;
import io.helidon.config.metadata.Configured;
import io.helidon.config.metadata.ConfiguredOption;
import io.helidon.pico.configdriven.api.ConfigBean;

/**
 * Server configuration bean.
 * See {@link io.helidon.nima.webserver.WebServer#create(java.util.function.Consumer)}.
 */
@Prototype.Blueprint(builderInterceptor = ServerConfigBlueprint.ServerConfigInterceptor.class)
@Configured(root = true, prefix = "server")
@ConfigBean(wantDefault = true)
interface ServerConfigBlueprint extends ListenerConfigBlueprint, Prototype.Factory<WebServer> {
    /**
     * When true the webserver registers a shutdown hook with the JVM Runtime.
     * <p>
     * Defaults to true. Set this to false such that a shutdown hook is not registered.
     *
     * @return whether to register a shutdown hook
     */
    @ConfiguredOption("true")
    boolean shutdownHook();

    /**
     * Socket configurations.
     * Note that socket named {@value io.helidon.nima.webserver.WebServer#DEFAULT_SOCKET_NAME} cannot be used,
     * configure the values on the server directly.
     *
     * @return map of listener configurations, except for the default one
     */
    @ConfiguredOption
    @Prototype.Singular
    Map<String, ListenerConfig> sockets();

    /**
     * Context for the WebServer, if none defined, a new one will be created with global context as the root.
     *
     * @return server context
     */
    Optional<Context> serverContext();

    class ServerConfigInterceptor implements Prototype.BuilderInterceptor<ServerConfig.BuilderBase<?, ?>> {
        @Override
        public ServerConfig.BuilderBase<?, ?> intercept(ServerConfig.BuilderBase<?, ?> target) {
            if (target.sockets().containsKey(WebServer.DEFAULT_SOCKET_NAME)) {
                throw new ConfigException("Default socket must be configured directly on server config node, or through"
                                                  + " \"ServerConfig.Builder\", not as a separated socket.");
            }

            return target;
        }
    }
}
