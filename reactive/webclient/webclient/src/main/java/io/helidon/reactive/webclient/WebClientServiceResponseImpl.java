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

import io.helidon.common.context.Context;
import io.helidon.common.http.Http;

/**
 * Implementation of {@link WebClientServiceResponse}.
 */
class WebClientServiceResponseImpl implements WebClientServiceResponse {

    private final Context context;
    private final WebClientResponseHeaders headers;
    private final Http.Status status;

    WebClientServiceResponseImpl(Context context, WebClientResponseHeaders headers, Http.Status status) {
        this.context = context;
        this.headers = headers;
        this.status = status;
    }

    @Override
    public WebClientResponseHeaders headers() {
        return headers;
    }

    @Override
    public Context context() {
        return context;
    }

    @Override
    public Http.Status status() {
        return status;
    }

}
