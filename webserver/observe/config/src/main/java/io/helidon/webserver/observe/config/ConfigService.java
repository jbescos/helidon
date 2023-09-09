/*
 * Copyright (c) 2022, 2023 Oracle and/or its affiliates.
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

package io.helidon.webserver.observe.config;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import io.helidon.common.config.Config;
import io.helidon.common.config.ConfigValue;
import io.helidon.common.config.GlobalConfig;
import io.helidon.http.NotFoundException;
import io.helidon.http.media.EntityWriter;
import io.helidon.http.media.jsonp.JsonpSupport;
import io.helidon.webserver.http.HttpRules;
import io.helidon.webserver.http.HttpService;
import io.helidon.webserver.http.SecureHandler;
import io.helidon.webserver.http.ServerRequest;
import io.helidon.webserver.http.ServerResponse;

import jakarta.json.Json;
import jakarta.json.JsonBuilderFactory;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

class ConfigService implements HttpService {
    private static final EntityWriter<JsonObject> WRITER = JsonpSupport.serverResponseWriter();
    private static final JsonBuilderFactory JSON = Json.createBuilderFactory(Map.of());

    private final List<Pattern> secretPatterns;
    private final String profile;

    private ConfigService(List<Pattern> secretPatterns) {
        this.secretPatterns = secretPatterns;
        this.profile = findProfile();
    }

    public static HttpService create(Config config) {
        List<String> configuredSecretsPatterns = config.get("secrets").asList(String.class).orElseGet(List::of);
        Set<String> secretsPatterns = new LinkedHashSet<>(configuredSecretsPatterns);
        secretsPatterns.add(".*password");
        secretsPatterns.add(".*passphrase");
        secretsPatterns.add(".*secret");
        List<Pattern> patterns = secretsPatterns.stream()
                .map(Pattern::compile)
                .toList();

        // todo we may want more configuration - use a builder?
        return new ConfigService(patterns);
    }

    @Override
    public void routing(HttpRules rules) {
        rules.any(SecureHandler.authorize("webserver-observe"))
                .get("/profile", this::profile)
                .get("/values", this::values)
                .get("/values/{name}", this::value);
    }

    private static String findProfile() {
        // we may want to have this directly in config
        String name = System.getenv("HELIDON_CONFIG_PROFILE");
        if (name != null) {
            return name;
        }
        name = System.getProperty("helidon.config.profile");
        if (name != null) {
            return name;
        }
        name = System.getProperty("config.profile");
        if (name != null) {
            return name;
        }
        return "";
    }

    private void value(ServerRequest req, ServerResponse res) {
        String name = req.path().pathParameters().get("name");

        ConfigValue<String> value = GlobalConfig.config().get(name).asString();
        if (value.isPresent()) {
            JsonObjectBuilder json = JSON.createObjectBuilder()
                    .add("name", name);

            json.add("value", obfuscate(name, value.get()));
            write(req, res, json.build());
        } else {
            throw new NotFoundException("Config value for key: " + name);
        }
    }

    private void values(ServerRequest req, ServerResponse res) {
        Map<String, String> mapOfValues = new HashMap<>(GlobalConfig.config().asMap()
                                                                .orElseGet(Map::of));

        JsonObjectBuilder json = JSON.createObjectBuilder();

        mapOfValues.forEach((key, value) -> json.add(key, obfuscate(key, value)));

        write(req, res, json.build());
    }

    private void profile(ServerRequest req, ServerResponse res) {
        JsonObject profile = JSON.createObjectBuilder()
                .add("name", this.profile)
                .build();

        write(req, res, profile);
    }

    private String obfuscate(String key, String value) {
        for (Pattern pattern : secretPatterns) {
            if (pattern.matcher(key).matches()) {
                return "*********";
            }
        }

        return value;
    }

    private void write(ServerRequest req, ServerResponse res, JsonObject json) {
        WRITER.write(JsonpSupport.JSON_OBJECT_TYPE,
                     json,
                     res.outputStream(),
                     req.headers(),
                     res.headers());
    }
}