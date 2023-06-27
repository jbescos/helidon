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

package io.helidon.builder.test.testsubjects;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Example of what would be code generated by the builder. Used in testing.
 */
@SuppressWarnings("unchecked")
public class MyConfigBeanManualImpl implements MyConfigBean {
    private static final Map<String, Map<String, Object>> metaProps = Collections.unmodifiableMap(calcMeta());

    private final String name;
    private final boolean enabled;
    private final int port;

    protected MyConfigBeanManualImpl(Builder builder) {
        this.name = builder.name;
        this.enabled = builder.enabled;
        this.port = builder.port;
    }

    private static Map<String, Map<String, Object>> calcMeta() {
        LinkedHashMap<String, Map<String, Object>> metaProps = new LinkedHashMap<>();
        metaProps.put("name", Map.of("required", "true"));
        metaProps.put("enabled", Collections.emptyMap());
        metaProps.put("port", Map.of("allowedValues", Arrays.asList("8080", "8081", "8082")));
        return metaProps;
    }

    public static Map<String, Map<String, Object>> getAttributesMeta() {
        return metaProps;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + toStringInner() + ")";
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, enabled, port);
    }

    @Override
    public boolean equals(Object another) {
        if (!(another instanceof MyConfigBean)) {
            return false;
        }
        MyConfigBean other = (MyConfigBean) another;
        boolean equals = true;
        equals &= Objects.equals(getName(), other.getName());
        equals &= Objects.equals(isEnabled(), other.isEnabled());
        equals &= Objects.equals(getPort(), other.getPort());
        return equals;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public int getPort() {
        return port;
    }

    protected CharSequence toStringInner() {
        return "name=" + getName() + ", "
                + "enabled=" + isEnabled() + ", "
                + "port=" + getPort();
    }

    public static Builder<? extends Builder> builder() {
        return new Builder();
    }

    public static Builder<? extends Builder> toBuilder(MyConfigBean val) {
        Builder b = new Builder();
        if (val != null) {
            b.name(val.getName());
            b.port(val.getPort());
            b.enabled(val.isEnabled());
        }
        return b;
    }

    public static class Builder<B extends Builder<B>> {
        private String name;
        private boolean enabled;
        private int port;

        public B name(String val) {
            this.name = val;
            return (B) this;
        }

        public B enabled(boolean val) {
            this.enabled = val;
            return (B) this;
        }

        public B port(int val) {
            this.port = val;
            return (B) this;
        }

        public MyConfigBean build() {
            return new MyConfigBeanManualImpl(this);
        }
    }

}
