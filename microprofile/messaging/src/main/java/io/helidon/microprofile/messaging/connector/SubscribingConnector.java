/*
 * Copyright (c)  2020 Oracle and/or its affiliates. All rights reserved.
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
 *
 */

package io.helidon.microprofile.messaging.connector;

import io.helidon.config.Config;

import org.eclipse.microprofile.reactive.messaging.Message;
import org.reactivestreams.Subscriber;

interface SubscribingConnector extends ConfigurableConnector {

    @Override
    default Config getChannelsConfig() {
        return getRootConfig().get("mp.messaging.outgoing");
    }

    Subscriber<? extends Message<?>> getSubscriber(String channelName);
}
