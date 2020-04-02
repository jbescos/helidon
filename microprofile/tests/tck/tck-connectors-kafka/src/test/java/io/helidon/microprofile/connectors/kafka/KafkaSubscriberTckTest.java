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

package io.helidon.microprofile.connectors.kafka;

import java.util.Arrays;
import java.util.UUID;

import org.apache.kafka.clients.producer.Producer;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.mockito.Mockito;
import org.reactivestreams.Subscriber;
import org.reactivestreams.tck.SubscriberBlackboxVerification;
import org.reactivestreams.tck.TestEnvironment;

class KafkaSubscriberTckTest extends SubscriberBlackboxVerification<Message<String>> {

    protected KafkaSubscriberTckTest() {
        super(new TestEnvironment(1000));
    }

    @Override
    public Message<String> createElement(int element) {
        return Message.of(UUID.randomUUID().toString());
    }

    @Override
    public Subscriber<Message<String>> createSubscriber() {
        Producer<?, String> producer = Mockito.mock(Producer.class);
        return KafkaSubscriber.build(Arrays.asList("topic"), 1, producer);
    }

}
