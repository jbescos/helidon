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

package io.helidon.integrations.micrometer;

import java.io.StringWriter;

import io.helidon.common.http.Http;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.nima.webserver.http.Handler;
import io.helidon.nima.webserver.http.ServerRequest;
import io.helidon.nima.webserver.http.ServerResponse;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.prometheus.PrometheusMeterRegistry;

/**
 * Handler for dealing with HTTP requests to the Micrometer endpoint that specify prometheus as the registry type.
 */
class NimaPrometheusHandler implements Handler {

    private final PrometheusMeterRegistry registry;

    private NimaPrometheusHandler(PrometheusMeterRegistry registry) {
        this.registry = registry;
    }

    static NimaPrometheusHandler create(MeterRegistry registry) {
        return new NimaPrometheusHandler(PrometheusMeterRegistry.class.cast(registry));
    }

    @Override
    public void handle(ServerRequest req, ServerResponse res) throws Exception {
        res.headers().contentType(MediaTypes.TEXT_PLAIN);

        Http.Method method = req.prologue().method();

        if (method == Http.Method.GET) {
            res.send(registry.scrape());
        } else if (method == Http.Method.OPTIONS) {
            StringWriter writer = new StringWriter();

            MicrometerPrometheusRegistrySupport.metadata(writer, registry);
            res.send(writer.toString());
        } else {
            res.status(Http.Status.NOT_IMPLEMENTED_501)
                    .send();
        }
    }
}
