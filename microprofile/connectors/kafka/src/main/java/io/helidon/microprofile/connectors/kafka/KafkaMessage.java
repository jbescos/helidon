/*
 * Copyright (c) 2020 Oracle and/or its affiliates. All rights reserved.
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

package io.helidon.microprofile.connectors.kafka;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.eclipse.microprofile.reactive.messaging.Message;

/**
 * Kafka specific MP messaging message.
 *
 * @param <K> kafka record key type
 * @param <V> kafka record value type
 */
class KafkaMessage<K, V> implements Message<ConsumerRecord<K, V>> {

    private static final Logger LOGGER = Logger.getLogger(KafkaMessage.class.getName());
    private final ConsumerRecord<K, V> consumerRecord;
    private final CompletableFuture<Void> kafkaCommit;
    private final long millisWaitingTimeout;
    // ACK is written and read from the same thread. It is not necessary to make it atomic.
    private boolean ack = false;

    /**
     * Kafka specific MP messaging message.
     *
     * @param consumerRecord {@link org.apache.kafka.clients.consumer.ConsumerRecord}
     * @param kafkaCommit it will complete when Kafka commit is done.
     * @param millisWaitingTimeout this is the time in milliseconds that the ack will be waiting
     *        the commit in Kafka. Applies only if autoCommit is false.
     */
    KafkaMessage(ConsumerRecord<K, V> consumerRecord, CompletableFuture<Void> kafkaCommit, long millisWaitingTimeout) {
        this.consumerRecord = consumerRecord;
        this.kafkaCommit = kafkaCommit;
        this.millisWaitingTimeout = millisWaitingTimeout;
    }

    @Override
    public ConsumerRecord<K, V> getPayload() {
        return consumerRecord;
    }

    @Override
    public CompletionStage<Void> ack() {
        ack = true;
        return kafkaCommit.orTimeout(millisWaitingTimeout, TimeUnit.MILLISECONDS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <C> C unwrap(Class<C> unwrapType) {
        if (consumerRecord.getClass().isAssignableFrom(unwrapType)) {
            return (C) consumerRecord;
        } else {
            throw new IllegalArgumentException("Can't unwrap to " + unwrapType.getName());
        }
    }

    boolean isAck() {
        return ack;
    }

    @Override
    public String toString() {
        return "KafkaMessage [consumerRecord=" + consumerRecord + ", ack=" + ack + "]";
    }

    CompletableFuture<Void> kafkaCommit(){
        return kafkaCommit;
    }

}
