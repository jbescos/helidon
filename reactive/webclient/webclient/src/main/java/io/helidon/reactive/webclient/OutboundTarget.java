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
package io.helidon.reactive.webclient;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import io.helidon.config.Config;

/**
 * Configuration of outbound target.
 * Outbound target is a set of protocols and hosts that share a configuration.
 * Name is considered to be a unique identification of a target, so it is used for hashCode and equals methods.
 */
final class OutboundTarget {
    /**
     * Configuration key for name of target.
     */
    public static final String CONFIG_NAME = "name";
    /**
     * Configuration key for string array of supported transports. If not provided or empty, all transports are supported
     */
    public static final String CONFIG_TRANSPORTS = "transports";
    /**
     * Configuration key for string array of hosts. If not provided or empty, all hosts are supported. If any host is the string
     * "*" (asterisk), all hosts are supported
     */
    public static final String CONFIG_HOSTS = "hosts";
    /**
     * Configuration key for string array of paths. If not provided or empty, all paths are supported. If any path is the string
     * "*" (asterisk), all paths are supported.
     */
    public static final String CONFIG_PATHS = "paths";

    private final String name;
    private final Set<String> transports = new HashSet<>();
    private final Set<String> hosts = new HashSet<>();
    private final List<Pattern> hostPatterns = new LinkedList<>();
    private final Set<String> paths = new HashSet<>();
    private final List<Pattern> pathPatterns = new LinkedList<>();
    private final Config config;

    private final boolean matchAllTransports;
    private final boolean matchAllHosts;
    private final boolean matchAllPaths;

    private OutboundTarget(Builder builder) {
        this.name = builder.name;
        this.transports.addAll(builder.transports);
        this.hosts.addAll(builder.hosts);
        this.paths.addAll(builder.paths);
        this.config = builder.config;

        matchAllTransports = this.transports.isEmpty() || anyMatch(this.transports);
        matchAllHosts = this.hosts.isEmpty() || anyMatch(this.hosts);
        matchAllPaths = this.paths.isEmpty() || anyMatch(this.paths);

        if (!matchAllHosts) {
            //only create patterns for hosts containing *
            this.hosts
                    .stream()
                    .filter(s -> s.contains("*"))
                    .forEach(host -> hostPatterns.add(toPattern(host)));
        }

        if (!matchAllPaths) {
            this.paths.forEach(path -> pathPatterns.add(Pattern.compile(path)));
        }
    }

    static OutboundTarget create(Config c) {
        Builder builder = new Builder();

        builder.config(c);
        builder.name(c.get(CONFIG_NAME).asString().get());
        c.get(CONFIG_TRANSPORTS).asList(String.class).orElse(List.of())
                .forEach(builder::addTransport);
        c.get(CONFIG_HOSTS).asList(String.class).orElse(List.of())
                .forEach(builder::addHost);
        c.get(CONFIG_PATHS).asList(String.class).orElse(List.of())
                .forEach(builder::addPath);

        return builder.build();
    }

    /**
     * Builder for a single target.
     *
     * @param name name of the target (if set to "default", all defaults from provider will be ignored)
     * @return Builder instance
     */
    public static Builder builder(String name) {
        return new Builder().name(name);
    }

    private Pattern toPattern(String host) {
        //host is very limited in dictionary - dots, alphanum and dashes...
        String pattern = host.replaceAll("\\.", "\\.");
        pattern = pattern.replaceAll("\\*", ".*");
        return Pattern.compile(pattern);
    }

    private boolean anyMatch(Set<String> values) {
        return values.contains("*");
    }

    /**
     * Name of this target.
     *
     * @return name
     */
    public String name() {
        return name;
    }

    /**
     * Transports of this target.
     *
     * @return transports this target is valid for
     */
    public Set<String> transports() {
        return Collections.unmodifiableSet(transports);
    }

    /**
     * Hosts of this target.
     *
     * @return hosts this target is valid for
     */
    public Set<String> hosts() {
        return Collections.unmodifiableSet(hosts);
    }

    /**
     * Configuration of this target.
     *
     * @return target configuration or empty if none provided
     */
    public Optional<Config> getConfig() {
        return Optional.ofNullable(config);
    }

    boolean matches(String transport, String host, String path) {
        return matchTransport(transport) && matchHost(host) && matchPath(path);
    }

    boolean matchPath(String path) {
        return match(path, matchAllPaths, paths, pathPatterns);
    }

    private boolean match(String toMatch, boolean matchAll, Set<String> values, List<Pattern> patterns) {
        if (matchAll) {
            return true;
        }

        if (null == toMatch) {
            return false;
        }

        if (values.contains(toMatch)) {
            return true;
        }

        for (Pattern pattern : patterns) {
            if (pattern.matcher(toMatch).matches()) {
                return true;
            }
        }

        return false;
    }

    boolean matchHost(String requestHost) {
        return match(requestHost, matchAllHosts, hosts, hostPatterns);
    }

    boolean matchTransport(String requestTransport) {
        if (matchAllTransports) {
            return true;
        }

        for (String transport : transports) {
            if (requestTransport.equals(transport)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        OutboundTarget that = (OutboundTarget) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return "TargetConfig{"
                + "name='" + name + '\''
                + ", transports=" + transports
                + ", hosts=" + hosts
                + ", hostPatterns=" + hostPatterns
                + ", config=" + config
                + ", matchAllTransports=" + matchAllTransports
                + ", matchAllHosts=" + matchAllHosts
                + '}';
    }

    /**
     * Fluent API builder for {@link OutboundTarget}.
     */
    public static final class Builder implements io.helidon.common.Builder<Builder, OutboundTarget> {
        private final Set<String> transports = new HashSet<>();
        private final Set<String> hosts = new HashSet<>();
        private final Set<String> paths = new HashSet<>();
        private String name;
        private Config config;

        private Builder() {
        }

        private Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Set config for this target. This may be useful if each target requires different
         * provider configuration.
         *
         * @param config Config object to configure the provider
         * @return update builder instance
         */
        public Builder config(Config config) {
            this.config = config;
            return this;
        }

        /**
         * Add supported host for this target. May be called more than once to add more hosts.
         * <p>
         * Valid examples:
         * <ul>
         * <li>localhost
         * <li>www.google.com
         * <li>127.0.0.1
         * <li>*.oracle.com
         * <li>192.169.*.*
         * <li>*.google.*
         * </ul>
         *
         * @param host name or IP of host, with possible "*" asterisk character to match any sequence
         * @return updated builder instance
         */
        public Builder addHost(String host) {
            this.hosts.add(host);
            return this;
        }

        /**
         * Add supported transports for this target. May be called more than once to add more transports.
         * <p>
         * Valid examples:
         * <ul>
         * <li>http
         * <li>https
         * </ul>
         * There is no wildcard support
         *
         * @param transport that is supported
         * @return updated builder instance
         */
        public Builder addTransport(String transport) {
            this.transports.add(transport);
            return this;
        }

        /**
         * Add supported paths for this target. May be called more than once to add more paths.
         * The path is tested as is against called path, and also tested as a regular expression.
         *
         * @param path supported path (regular expression supported)
         * @return updated builder instance
         */
        public Builder addPath(String path) {
            this.paths.add(path);
            return this;
        }

        /**
         * Build a {@link OutboundTarget} instance from this builder.
         *
         * @return instance configured with this builder values
         */
        @Override
        public OutboundTarget build() {
            return new OutboundTarget(this);
        }
    }
}
