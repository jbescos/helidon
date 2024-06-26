/*
 * Copyright (c) 2018, 2022 Oracle and/or its affiliates.
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
package io.helidon.microprofile.tracing;

import java.util.Optional;

import io.helidon.common.context.Contexts;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.tracing.Span;
import io.helidon.tracing.SpanContext;
import io.helidon.tracing.Tracer;
import io.helidon.tracing.jersey.client.internal.TracingContext;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Provider;
import jakarta.ws.rs.ConstrainedTo;
import jakarta.ws.rs.RuntimeType;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Context;
import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

/**
 * Automatically registered filter that stores
 * required information in thread local, to allow outbound clients to get
 * all context.
 *
 * @see TracingContext
 */
@ConstrainedTo(RuntimeType.SERVER)
@PreMatching
@Priority(Integer.MIN_VALUE)
@ApplicationScoped
public class MpTracingContextFilter implements ContainerRequestFilter {
    private final Provider<ServerRequest> request;

    private final Config config = ConfigProvider.getConfig();

    /**
     * Constructor to be used by JAX-RS implementation.
     *
     * @param request injected by JAX-RS
     */
    public MpTracingContextFilter(@Context Provider<ServerRequest> request) {
        this.request = request;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) {
        ServerRequest serverRequest = this.request.get();

        Tracer tracer = serverRequest.context().get(Tracer.class).orElseGet(Tracer::global);
        Optional<SpanContext> parentSpan = Span.current().map(Span::context);

        boolean clientEnabled = config.getOptionalValue("tracing.client.enabled", Boolean.class).orElse(true);
        TracingContext tracingContext = TracingContext.create(tracer, serverRequest.headers().toMap(), clientEnabled);
        parentSpan.ifPresent(tracingContext::parentSpan);

        Contexts.context().ifPresent(ctx -> ctx.register(tracingContext));
    }
}
