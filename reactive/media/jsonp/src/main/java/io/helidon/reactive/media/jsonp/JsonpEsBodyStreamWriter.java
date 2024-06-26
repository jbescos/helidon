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
package io.helidon.reactive.media.jsonp;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Flow;

import io.helidon.common.GenericType;
import io.helidon.common.http.DataChunk;
import io.helidon.common.http.HttpMediaType;
import io.helidon.common.media.type.MediaType;
import io.helidon.common.media.type.MediaTypes;
import io.helidon.common.reactive.Multi;
import io.helidon.reactive.media.common.MessageBodyStreamWriter;
import io.helidon.reactive.media.common.MessageBodyWriterContext;
import io.helidon.reactive.media.jsonp.JsonpBodyWriter.JsonStructureToChunks;

import jakarta.json.JsonStructure;
import jakarta.json.JsonWriterFactory;

/**
 * Message body writer for {@link jakarta.json.JsonStructure} sub-classes (JSON-P).
 * This writer is for {@link MediaTypes#TEXT_EVENT_STREAM} with no element-type parameter or element-type="application/json".
 */
class JsonpEsBodyStreamWriter implements MessageBodyStreamWriter<JsonStructure> {

    private static final HttpMediaType TEXT_EVENT_STREAM_JSON = HttpMediaType
            .create("text/event-stream;element-type=\"application/json\"");
    private static final byte[] DATA = "data: ".getBytes(StandardCharsets.UTF_8);
    private static final byte[] NL = "\n\n".getBytes(StandardCharsets.UTF_8);

    private final JsonWriterFactory jsonWriterFactory;

    JsonpEsBodyStreamWriter(JsonWriterFactory jsonWriterFactory) {
        this.jsonWriterFactory = Objects.requireNonNull(jsonWriterFactory);
    }

    @Override
    public PredicateResult accept(GenericType<?> type, MessageBodyWriterContext context) {
        if (!JsonStructure.class.isAssignableFrom(type.rawType())) {
            return PredicateResult.NOT_SUPPORTED;
        }
        return context.contentType()
                .or(() -> findMediaType(context))
                .filter(mediaType -> mediaType.test(TEXT_EVENT_STREAM_JSON) || mediaType.test(MediaTypes.TEXT_EVENT_STREAM))
                .map(it -> PredicateResult.COMPATIBLE)
                .orElse(PredicateResult.NOT_SUPPORTED);
    }

    @Override
    public Multi<DataChunk> write(Flow.Publisher<? extends JsonStructure> publisher,
                                  GenericType<? extends JsonStructure> type,
                                  MessageBodyWriterContext context) {

        MediaType contentType = context.contentType()
                .or(() -> findMediaType(context))
                .orElse(TEXT_EVENT_STREAM_JSON);

        context.contentType(contentType);

        JsonStructureToChunks jsonToChunks = new JsonStructureToChunks(true,
                                                                       jsonWriterFactory,
                                                                       context.charset());

        return Multi.create(publisher)
                .map(jsonToChunks)
                .flatMap(dataChunk -> Multi.just(
                        DataChunk.create(DATA),
                        dataChunk,
                        DataChunk.create(NL)));
    }

    private Optional<HttpMediaType> findMediaType(MessageBodyWriterContext context) {
        try {
            return Optional.of(context.findAccepted(HttpMediaType.JSON_EVENT_STREAM_PREDICATE, TEXT_EVENT_STREAM_JSON));
        } catch (IllegalStateException ignore) {
            //Not supported. Ignore exception.
            return Optional.empty();
        }
    }

}
