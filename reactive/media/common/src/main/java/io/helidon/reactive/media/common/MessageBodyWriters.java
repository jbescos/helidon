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
package io.helidon.reactive.media.common;

/**
 * Registry of {@link MessageBodyWriter} allowing to register writer instances
 * in the system.
 */
public interface MessageBodyWriters {

    /**
     * Register a writer.
     *
     * @param writer writer to register
     * @return Writers
     */
    MessageBodyWriters registerWriter(MessageBodyWriter<?> writer);

    /**
     * Register a stream writer.
     *
     * @param writer writer to register
     * @return Writers
     */
    MessageBodyWriters registerWriter(MessageBodyStreamWriter<?> writer);
}
