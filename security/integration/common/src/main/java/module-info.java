/*
 * Copyright (c) 2017, 2023 Oracle and/or its affiliates.
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

/**
 * Security integration common.
 */
module io.helidon.security.integration.common {

    requires io.helidon.common.context;
    requires java.logging;

    requires transitive io.helidon.security;
    requires transitive io.helidon.tracing.config;
    requires transitive io.helidon.tracing;

    exports io.helidon.security.integration.common;

}
