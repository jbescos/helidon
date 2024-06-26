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
package io.helidon.reactive.webserver.examples.basics;

import java.util.concurrent.Flow;

import io.helidon.common.GenericType;
import io.helidon.common.http.DataChunk;
import io.helidon.common.http.HttpMediaType;
import io.helidon.common.reactive.Single;
import io.helidon.reactive.media.common.ContentReaders;
import io.helidon.reactive.media.common.MessageBodyReader;
import io.helidon.reactive.media.common.MessageBodyReaderContext;

/**
 * Reader for the custom media type.
 */
public class NameReader implements MessageBodyReader<Name> {

    static final HttpMediaType APP_NAME = HttpMediaType.create("application/name");

    private NameReader() {
    }

    static NameReader create() {
        return new NameReader();
    }

    @Override
    public <U extends Name> Single<U> read(Flow.Publisher<DataChunk> publisher, GenericType<U> type,
                                           MessageBodyReaderContext context) {
        return (Single<U>) ContentReaders.readString(publisher, context.charset()).map(Name::new);
    }

    @Override
    public PredicateResult accept(GenericType<?> type, MessageBodyReaderContext context) {
        return context.contentType()
                .filter(APP_NAME::equals)
                .map(it -> PredicateResult.supports(Name.class, type))
                .orElse(PredicateResult.NOT_SUPPORTED);
    }
}




