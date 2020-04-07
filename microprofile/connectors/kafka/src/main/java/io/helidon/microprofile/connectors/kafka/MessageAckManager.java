/*
 * Copyright (c)  2020 Oracle and/or its affiliates.
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;

public class MessageAckManager<K, V> {

    private final LinkedList<KafkaMessage<K, V>> queue = new LinkedList<>();
    private final long ackBufferSize;
    private final Consumer<K, V> kafkaConsumer;
    private final ReentrantLock consumerLock;

    public MessageAckManager(final long ackBufferSize, final Consumer<K, V> kafkaConsumer, final ReentrantLock consumerLock) {
        this.ackBufferSize = ackBufferSize;
        this.kafkaConsumer = kafkaConsumer;
        this.consumerLock = consumerLock;
    }

    KafkaMessage<K, V> createKafkaMessage(ConsumerRecord<K, V> consumerRecord) {
        return new KafkaMessage<>(consumerRecord, new AckCallBack<>(this));
    }

    void register(KafkaMessage<K, V> kafkaMessage){
        queue.addLast(kafkaMessage);
    }

    boolean ackQueueOverflown(){
        return queue.size() > ackBufferSize;
    }

    void tryCommit() {
        consumerLock(() -> {
            Map<TopicPartition, OffsetAndMetadata> ackedOffsets = new HashMap<>();
            Queue<CompletableFuture<Void>> commitFutures = new LinkedList<>();
            // Gather all acked messages and their futures returned by Message.ack()
            for (KafkaMessage<K, V> kf = queue.poll();
                 kf != null && kf.isAcked();
                 kf = queue.poll()) {

                commitFutures.add(kf.getCallback().getCommitFuture());
                ConsumerRecord<K, V> cr = kf.getPayload();
                ackedOffsets.put(new TopicPartition(cr.topic(), cr.partition()), new OffsetAndMetadata(cr.offset()));
            }
            // Commit and don't wait for result
            kafkaConsumer.commitAsync(ackedOffsets, (offsets, exception) -> {
                consumerLock(() -> {
                    // Commit is confirmed, complete all futures returned by Message.ack()
                    for (CompletableFuture<Void> future = commitFutures.poll();
                         future != null;
                         future = commitFutures.poll()) {

                        if (exception == null) {
                            future.completeAsync(() -> null);
                        } else {
                            future.completeExceptionally(exception);
                        }
                    }
                });
            });
        });
    }

    static class AckCallBack<K, V> {

        private final CompletableFuture<Void> commitFuture = new CompletableFuture<>();
        private final MessageAckManager<K, V> ackManager;

        public AckCallBack(MessageAckManager<K, V> ackManager) {
            this.ackManager = ackManager;
        }

        CompletableFuture<Void> getCommitFuture() {
            return commitFuture;
        }

        CompletionStage<Void> ack(KafkaMessage<K, V> kafkaMessage) {
            ackManager.tryCommit();
            return commitFuture;
        }
    }

    private void consumerLock(Runnable runnable) {
        try {
            consumerLock.lock();
            runnable.run();
        } finally {
            consumerLock.unlock();
        }
    }
}
