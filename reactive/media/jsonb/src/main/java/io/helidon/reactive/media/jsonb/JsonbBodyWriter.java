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
package io.helidon.reactive.media.jsonb;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.concurrent.Flow.Publisher;

import io.helidon.common.GenericType;
import io.helidon.common.http.DataChunk;
import io.helidon.common.http.HttpMediaType;
import io.helidon.common.mapper.Mapper;
import io.helidon.common.reactive.Single;
import io.helidon.reactive.media.common.CharBuffer;
import io.helidon.reactive.media.common.ContentWriters;
import io.helidon.reactive.media.common.MessageBodyWriter;
import io.helidon.reactive.media.common.MessageBodyWriterContext;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbException;

/**
 * Message body writer supporting object binding with JSON-B.
 */
class JsonbBodyWriter implements MessageBodyWriter<Object> {

    private final Jsonb jsonb;

    private JsonbBodyWriter(Jsonb jsonb) {
        Objects.requireNonNull(jsonb);
        this.jsonb = jsonb;
    }

    @Override
    public PredicateResult accept(GenericType<?> type,
                                  MessageBodyWriterContext context) {
        return !CharSequence.class.isAssignableFrom(type.rawType())
                ? PredicateResult.COMPATIBLE
                : PredicateResult.NOT_SUPPORTED;
    }

    @Override
    public Publisher<DataChunk> write(Single<? extends Object> content,  GenericType<? extends Object> type,
            MessageBodyWriterContext context) {

        HttpMediaType contentType = context.findAccepted(HttpMediaType.JSON_PREDICATE, HttpMediaType.APPLICATION_JSON);
        context.contentType(contentType);
        return content.flatMap(new ObjectToChunks(jsonb, context.charset()));
    }

    /**
     * Create a new {@link JsonbBodyWriter} instance.
     *
     * @param jsonb JSON-B instance
     * @return JsonbBodyWriter
     */
    public static JsonbBodyWriter create(Jsonb jsonb) {
        return new JsonbBodyWriter(jsonb);
    }

    /**
     * Implementation of {@link Mapper} that converts objects into chunks.
     */
    static final class ObjectToChunks implements Mapper<Object, Publisher<DataChunk>> {

        private final Jsonb jsonb;
        private final Charset charset;

        ObjectToChunks(Jsonb jsonb, Charset charset) {
            this.jsonb = jsonb;
            this.charset = charset;
        }

        @Override
        public Publisher<DataChunk> map(Object item) {
            CharBuffer buffer = new CharBuffer();
            try {
                jsonb.toJson(item, buffer);
                return ContentWriters.writeCharBuffer(buffer, charset);
            } catch (IllegalStateException | JsonbException ex) {
                return Single.<DataChunk>error(ex);
            }
        }
    }
}
