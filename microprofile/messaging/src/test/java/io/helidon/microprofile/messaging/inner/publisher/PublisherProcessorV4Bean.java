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

package io.helidon.microprofile.messaging.inner.publisher;

import io.helidon.microprofile.messaging.CountableTestBean;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;
import org.eclipse.microprofile.reactive.streams.operators.PublisherBuilder;
import org.eclipse.microprofile.reactive.streams.operators.ReactiveStreams;

import javax.enterprise.context.ApplicationScoped;

import java.util.concurrent.CountDownLatch;

@ApplicationScoped
public class PublisherProcessorV4Bean implements CountableTestBean {

    public static CountDownLatch testLatch = new CountDownLatch(10);

    @Outgoing("publisher-for-processor-publisher-builder-payload")
    public PublisherBuilder<Integer> streamForProcessorBuilderOfPayloads() {
        return ReactiveStreams.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
    }

    @Incoming("publisher-for-processor-publisher-builder-payload")
    @Outgoing("processor-publisher-builder-payload")
    public PublisherBuilder<String> processorBuilderOfPayloads(int value) {
        return ReactiveStreams.of(value)
                .map(i -> i + 1)
                .flatMap(i -> ReactiveStreams.of(i, i))
                .map(i -> Integer.toString(i));
    }

    @Incoming("processor-publisher-builder-payload")
    public void getMessgesFromProcessorBuilderOfPayloads(String value) {
        getTestLatch().countDown();
    }

    @Override
    public CountDownLatch getTestLatch() {
        return testLatch;
    }
}
