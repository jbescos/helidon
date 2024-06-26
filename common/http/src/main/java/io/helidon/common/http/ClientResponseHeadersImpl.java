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

package io.helidon.common.http;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

class ClientResponseHeadersImpl implements ClientResponseHeaders {
    private final Headers headers;

    ClientResponseHeadersImpl(Headers headers) {
        this.headers = headers;
    }

    @Override
    public List<String> all(Http.HeaderName name, Supplier<List<String>> defaultSupplier) {
        return headers.all(name, defaultSupplier);
    }

    @Override
    public boolean contains(Http.HeaderName name) {
        return headers.contains(name);
    }

    @Override
    public boolean contains(Http.HeaderValue headerWithValue) {
        return headers.contains(headerWithValue);
    }

    @Override
    public Http.HeaderValue get(Http.HeaderName name) {
        return headers.get(name);
    }

    @Override
    public int size() {
        return headers.size();
    }

    @Override
    public Iterator<Http.HeaderValue> iterator() {
        return headers.iterator();
    }

    @Override
    public List<HttpMediaType> acceptedTypes() {
        return headers.acceptedTypes();
    }

    @Override
    public String toString() {
        return headers.toString();
    }
}
