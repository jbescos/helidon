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

import java.util.Collections;
import java.util.Map;

import io.helidon.config.Config;
import io.helidon.config.ConfigValue;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterRegistryConfig;
import io.micrometer.prometheus.PrometheusConfig;

/**
 * Framework for supporting Micrometer registry types.
 *
 */
public abstract class MicrometerBuiltInRegistrySupport {
    abstract static class AbstractMeterRegistryConfig implements MeterRegistryConfig {
        private final Map<String, String> settings;

        AbstractMeterRegistryConfig(Config config) {
            settings = config
                    .detach()
                    .asMap()
                    .orElse(Collections.emptyMap());
        }

        @Override
        public String get(String key) {
            return settings.get(key);
        }
    }

    static MicrometerBuiltInRegistrySupport create(BuiltInRegistryType type,
            ConfigValue<Config> node) {
        switch (type) {
            case PROMETHEUS:
                return create(type, PrometheusConfigImpl.registryConfig(node));

            default:
                throw new IllegalArgumentException(unrecognizedMessage(type));
        }
    }

    static MicrometerBuiltInRegistrySupport create(BuiltInRegistryType type,
            MeterRegistryConfig meterRegistryConfig) {
        switch (type) {
            case PROMETHEUS:
                return new MicrometerPrometheusRegistrySupport(meterRegistryConfig);

            default:
                throw new IllegalArgumentException(unrecognizedMessage(type));
        }
    }

    static MicrometerBuiltInRegistrySupport create(BuiltInRegistryType type) {
        MeterRegistryConfig meterRegistryConfig;
        switch (type) {
            case PROMETHEUS:
                meterRegistryConfig = PrometheusConfig.DEFAULT;
                break;

            default:
                throw new IllegalArgumentException(unrecognizedMessage(type));
        }
        return create(type, meterRegistryConfig);
    }

    private final MeterRegistry registry;

    protected MicrometerBuiltInRegistrySupport(MeterRegistryConfig meterRegistryConfig) {
        registry = createRegistry(meterRegistryConfig);
    }

    protected abstract MeterRegistry createRegistry(MeterRegistryConfig meterRegistryConfig);

    public MeterRegistry registry() {
        return registry;
    }

    protected static String unrecognizedMessage(BuiltInRegistryType type) {
        return String.format("Built-in registry type %s recognized but no support found", type.name());
    }

    /**
     * Prometheus configuration.
     *
     */
    private static class PrometheusConfigImpl extends AbstractMeterRegistryConfig implements PrometheusConfig {

        /**
         * Obtain a PrometheusConfig from the configuration.
         * @param node with the configuration
         * @return an instance of configured PrometheusConfig
         */
        public static PrometheusConfig registryConfig(ConfigValue<Config> node) {
            return node
                    .filter(Config::exists)
                    .map(PrometheusConfigImpl::registryConfig)
                    .orElse(PrometheusConfig.DEFAULT);
        }

        /**
         * Obtain a PrometheusConfig from the configuration.
         * @param config with the configuration
         * @return an instance of configured PrometheusConfig
         */
        public static PrometheusConfig registryConfig(Config config) {
            return new PrometheusConfigImpl(config);
        }

        private PrometheusConfigImpl(Config config) {
            super(config);
        }
    }
}
