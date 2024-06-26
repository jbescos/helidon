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
package io.helidon.reactive.media.multipart;

import io.helidon.reactive.media.common.MessageBodyContent;
import io.helidon.reactive.media.common.MessageBodyWriterContext;


/**
 * Writeable body part content.
 */
public interface WriteableBodyPartContent extends MessageBodyContent {

    /**
     * Initialize the body part content.
     * @param context writer context to use to initialize the underlying publisher
     * @return this body part content
     */
    WriteableBodyPartContent init(MessageBodyWriterContext context);
}
