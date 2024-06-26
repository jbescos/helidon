/*
 * Copyright (c) 2019, 2021 Oracle and/or its affiliates.
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
package io.helidon.tracing.zipkin;

import brave.opentracing.BraveScopeManager;
import io.opentracing.Scope;
import io.opentracing.ScopeManager;
import io.opentracing.Span;

/**
 * Scope manager delegating to zipkin.
 * We need to "unwrap" the span before activating it, as otherwise we got a class cast exception.
 */
class ZipkinScopeManager implements ScopeManager {
    private final BraveScopeManager scopeManager;

    ZipkinScopeManager(BraveScopeManager scopeManager) {
        this.scopeManager = scopeManager;
    }

    @Override
    public Scope activate(Span span) {
        return scopeManager.activate(unwrap(span));
    }

    private Span unwrap(Span span) {
        Span unwrapped = span;

        if (span instanceof ZipkinSpan) {
            unwrapped = ((ZipkinSpan) span).unwrap();
        }

        return unwrapped;
    }

    @Override
    public Span activeSpan() {
        return scopeManager.activeSpan();
    }
}
