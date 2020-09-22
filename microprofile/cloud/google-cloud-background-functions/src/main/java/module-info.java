/*
 * Copyright (c) 2020 Oracle and/or its affiliates.
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
 * Google Cloud Functions Background.
 */
module io.helidon.microprofile.cloud.googlecloudfunctions.background {
    requires java.logging;
    requires io.helidon.common;
    requires transitive functions.framework.api;
    requires io.helidon.microprofile.cloud.common;
    requires java.json.bind;

    exports io.helidon.microprofile.cloud.googlecloudfunctions.background;
}
