/*
 * Copyright (c) 2021, 2023 Oracle and/or its affiliates.
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

import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;

import io.micrometer.core.instrument.config.MeterRegistryConfig;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.Collector;

/**
 * Support for built-in Prometheus meter registry type.
 */
public final class MicrometerPrometheusRegistrySupport extends MicrometerBuiltInRegistrySupport {

    MicrometerPrometheusRegistrySupport(MeterRegistryConfig meterRegistryConfig) {
        super(meterRegistryConfig);
    }

    @Override
    protected PrometheusMeterRegistry createRegistry(MeterRegistryConfig meterRegistryConfig) {
        return new PrometheusMeterRegistry(PrometheusConfig.class.cast(meterRegistryConfig));
    }

    /**
     * Utility method to write the PrometheusMeterRegistry in a writer.
     * @param writer any subclass of Writer
     * @param registry the PrometheusMeterRegistry
     * @throws IOException if an I/O error occurs
     */
    public static void metadata(Writer writer, PrometheusMeterRegistry registry) throws IOException {
        Enumeration<Collector.MetricFamilySamples> mfs = registry.getPrometheusRegistry()
                .metricFamilySamples();
        while (mfs.hasMoreElements()) {
            Collector.MetricFamilySamples metricFamilySamples = mfs.nextElement();
            writer.write("# HELP ");
            writer.write(metricFamilySamples.name);
            writer.write(' ');
            writeEscapedHelp(writer, metricFamilySamples.help);
            writer.write('\n');

            writer.write("# TYPE ");
            writer.write(metricFamilySamples.name);
            writer.write(' ');
            writer.write(typeString(metricFamilySamples.type));
            writer.write('\n');
        }
    }

    private static void writeEscapedHelp(Writer writer, String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '\\':
                    writer.append("\\\\");
                    break;
                case '\n':
                    writer.append("\\n");
                    break;
                default:
                    writer.append(c);
            }
        }
    }

    private static String typeString(Collector.Type t) {
        switch (t) {
            case GAUGE:
                return "gauge";
            case COUNTER:
                return "counter";
            case SUMMARY:
                return "summary";
            case HISTOGRAM:
                return "histogram";
            default:
                return "untyped";
        }
    }
}
