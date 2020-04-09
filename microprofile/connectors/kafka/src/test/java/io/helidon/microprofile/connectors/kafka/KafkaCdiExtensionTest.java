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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.salesforce.kafka.test.junit5.SharedKafkaTestResource;

import java.io.Closeable;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.enterprise.inject.Instance;
import javax.enterprise.inject.se.SeContainer;
import javax.enterprise.inject.se.SeContainerInitializer;

import io.helidon.config.Config;
import io.helidon.config.ConfigSources;
import io.helidon.config.MpConfigProviderResolver;
import io.helidon.microprofile.messaging.MessagingCdiExtension;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.eclipse.microprofile.reactive.messaging.spi.Connector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class KafkaCdiExtensionTest {

    private static final Logger LOGGER = Logger.getLogger(KafkaCdiExtensionTest.class.getName());
    protected SeContainer cdiContainer;

    protected static final Connector KAFKA_CONNECTOR_LITERAL = new Connector() {

        @Override
        public Class<? extends Annotation> annotationType() {
            return Connector.class;
        }

        @Override
        public String value() {
            return KafkaConnector.CONNECTOR_NAME;
        }
    };

    @RegisterExtension
    public static final SharedKafkaTestResource kafkaResource = new SharedKafkaTestResource();
    public static final String TEST_TOPIC_1 = "graph-done-1";
    public static final String TEST_TOPIC_2 = "graph-done-2";
    public static final String TEST_TOPIC_3 = "graph-done-3";
    public static final String TEST_TOPIC_4 = "graph-done-4";
    public static final String TEST_TOPIC_5 = "graph-done-5";
    public static final String TEST_TOPIC_6 = "graph-done-6";
    public static final String TEST_TOPIC_SHORT_POLL_TIMEOUT = "short_poll_timeout";

    protected Map<String, String> cdiConfig() {
        Map<String, String> p = new HashMap<>();
        p.putAll(Map.of(
                "mp.messaging.incoming.test-channel-1.poll.timeout", "10",
                "mp.messaging.incoming.test-channel-1.period.executions", "10",
                "mp.messaging.incoming.test-channel-1.connector", KafkaConnector.CONNECTOR_NAME,
                "mp.messaging.incoming.test-channel-1.bootstrap.servers", kafkaResource.getKafkaConnectString(),
                "mp.messaging.incoming.test-channel-1.topic", TEST_TOPIC_1,
                "mp.messaging.incoming.test-channel-1.group.id", "group1",
                "mp.messaging.incoming.test-channel-1.key.deserializer", LongDeserializer.class.getName(),
                "mp.messaging.incoming.test-channel-1.value.deserializer", StringDeserializer.class.getName()));
        p.putAll(Map.of(
                "mp.messaging.incoming.test-channel-2.connector", KafkaConnector.CONNECTOR_NAME,
                "mp.messaging.incoming.test-channel-2.bootstrap.servers", kafkaResource.getKafkaConnectString(),
                "mp.messaging.incoming.test-channel-2.topic", TEST_TOPIC_2,
                "mp.messaging.incoming.test-channel-2.group.id", "group2",
                "mp.messaging.incoming.test-channel-2.key.deserializer", LongDeserializer.class.getName(),
                "mp.messaging.incoming.test-channel-2.value.deserializer", StringDeserializer.class.getName())
        );
        p.putAll(Map.of(
                "mp.messaging.outgoing.test-channel-3.connector", KafkaConnector.CONNECTOR_NAME,
                "mp.messaging.outgoing.test-channel-3.bootstrap.servers", kafkaResource.getKafkaConnectString(),
                "mp.messaging.outgoing.test-channel-3.topic", TEST_TOPIC_1,
                "mp.messaging.outgoing.test-channel-3.backpressure.size", "5",
                "mp.messaging.outgoing.test-channel-3.key.serializer", LongSerializer.class.getName(),
                "mp.messaging.outgoing.test-channel-3.value.serializer", StringSerializer.class.getName())
        );
        p.putAll(Map.of(
                "mp.messaging.incoming.test-channel-error.connector", KafkaConnector.CONNECTOR_NAME,
                "mp.messaging.incoming.test-channel-error.bootstrap.servers", kafkaResource.getKafkaConnectString(),
                "mp.messaging.incoming.test-channel-error.topic", TEST_TOPIC_3,
                "mp.messaging.incoming.test-channel-error.group.id", "group3",
                "mp.messaging.incoming.test-channel-error.key.deserializer", LongDeserializer.class.getName(),
                "mp.messaging.incoming.test-channel-error.value.deserializer", StringDeserializer.class.getName())
        );
        p.putAll(Map.of(
                "mp.messaging.incoming.test-channel-4.poll.timeout", "10",
                "mp.messaging.incoming.test-channel-4.period.executions", "10",
                "mp.messaging.incoming.test-channel-4.connector", KafkaConnector.CONNECTOR_NAME,
                "mp.messaging.incoming.test-channel-4.bootstrap.servers", kafkaResource.getKafkaConnectString(),
                "mp.messaging.incoming.test-channel-4.topic", TEST_TOPIC_4,
                "mp.messaging.incoming.test-channel-4.group.id", "group4",
                "mp.messaging.incoming.test-channel-4.key.deserializer", LongDeserializer.class.getName(),
                "mp.messaging.incoming.test-channel-4.value.deserializer", StringDeserializer.class.getName()));
        p.putAll(Map.of(
                "mp.messaging.incoming.test-channel-5.connector", KafkaConnector.CONNECTOR_NAME,
                "mp.messaging.incoming.test-channel-5.bootstrap.servers", kafkaResource.getKafkaConnectString(),
                "mp.messaging.incoming.test-channel-5.topic", TEST_TOPIC_5,
                "mp.messaging.incoming.test-channel-5.group.id", "group4",
                "mp.messaging.incoming.test-channel-5.key.deserializer", LongDeserializer.class.getName(),
                "mp.messaging.incoming.test-channel-5.value.deserializer", StringDeserializer.class.getName()));
        p.putAll(Map.of(
                "mp.messaging.incoming.test-channel-6.connector", KafkaConnector.CONNECTOR_NAME,
                "mp.messaging.incoming.test-channel-6.bootstrap.servers", kafkaResource.getKafkaConnectString(),
                "mp.messaging.incoming.test-channel-6.topic", TEST_TOPIC_6,
                "mp.messaging.incoming.test-channel-6.group.id", "group4",
                "mp.messaging.incoming.test-channel-6.enable.auto.commit", "false",
                "mp.messaging.incoming.test-channel-6.key.deserializer", LongDeserializer.class.getName(),
                "mp.messaging.incoming.test-channel-6.value.deserializer", StringDeserializer.class.getName()));
        p.putAll(Map.of(
                "mp.messaging.incoming.short-poll-timeout-channel.connector", KafkaConnector.CONNECTOR_NAME,
                "mp.messaging.incoming.short-poll-timeout-channel.bootstrap.servers", kafkaResource.getKafkaConnectString(),
                "mp.messaging.incoming.short-poll-timeout-channel.topic", TEST_TOPIC_SHORT_POLL_TIMEOUT,
                "mp.messaging.incoming.short-poll-timeout-channel.group.id", "group6",
                "mp.messaging.incoming.short-poll-timeout-channel.key.deserializer", LongDeserializer.class.getName(),
                "mp.messaging.incoming.short-poll-timeout-channel.value.deserializer", StringDeserializer.class.getName()));
        return p;
    }

    @BeforeAll
    static void prepareTopics() {
        kafkaResource.getKafkaTestUtils().createTopic(TEST_TOPIC_1, 10, (short) 1);
        kafkaResource.getKafkaTestUtils().createTopic(TEST_TOPIC_2, 10, (short) 1);
        kafkaResource.getKafkaTestUtils().createTopic(TEST_TOPIC_3, 10, (short) 1);
        kafkaResource.getKafkaTestUtils().createTopic(TEST_TOPIC_4, 10, (short) 1);
        kafkaResource.getKafkaTestUtils().createTopic(TEST_TOPIC_5, 10, (short) 1);
    }

    @BeforeEach
    void setUp() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(KafkaConnector.class);
        classes.add(KafkaSampleBean.class);
        classes.add(KafkaNoFullAck1Bean.class);
        classes.add(KafkaNoFullAck2Bean.class);
        classes.add(MessagingCdiExtension.class);

        Map<String, String> p = new HashMap<>(cdiConfig());
        cdiContainer = startCdiContainer(p, classes);
        assertTrue(cdiContainer.isRunning());

        //Wait till consumers are ready
        getInstance(KafkaConnector.class, KAFKA_CONNECTOR_LITERAL).stream()
                .flatMap(factory -> factory.resources().stream())
                .filter(closeable -> closeable instanceof KafkaPublisher).forEach(c -> {
            try {
                LOGGER.log(Level.FINE, "Waiting for Kafka topic");
                ((KafkaPublisher) c).waitForPartitionAssigment(10, TimeUnit.SECONDS);
            } catch (InterruptedException | TimeoutException e) {
                fail(e);
            }
        });
        LOGGER.info("Container started");
    }

    @AfterEach
    void tearDown() {
        KafkaConnector factory = getInstance(KafkaConnector.class, KAFKA_CONNECTOR_LITERAL).get();
        Collection<Closeable> resources = factory.resources();
        assertFalse(resources.isEmpty());
        cdiContainer.close();
        assertTrue(resources.isEmpty());
        LOGGER.info("Container destroyed");
    }

    @Test
    void multipleTopics() {
        LOGGER.fine("==========> test multipleTopics()");
        Map<String, String> p = Map.of("topic", "topic1,topic2");
        Config config = Config.builder().sources(ConfigSources.create(p)).build();
        Map<String, Object> kafkaProperties = KafkaConfigUtils.toMap(config);
        List<String> topics = KafkaConfigUtils.topicNameList(kafkaProperties);
        assertEquals(2, topics.size());
        assertTrue(topics.containsAll(Arrays.asList("topic1", "topic2")));
    }

    @Test
    void incomingKafkaOk() {
        LOGGER.fine("==========> test incomingKafkaOk()");
        List<String> testData = IntStream.range(0, 999).mapToObj(i -> "test" + i).collect(Collectors.toList());
        CountDownLatch testChannelLatch = new CountDownLatch(testData.size());
        KafkaSampleBean kafkaConsumingBean = cdiContainer.select(KafkaSampleBean.class).get();
        kafkaConsumingBean.setCountDownLatch(testChannelLatch);
        produceAndCheck(kafkaConsumingBean, testData, TEST_TOPIC_1, testChannelLatch, testData);
    }

    @Test
    void processor() {
        LOGGER.fine("==========> test processor()");
        // This test pushes in topic 2, it is processed and 
        // pushed in topic 1, and finally check the results coming from topic 1.
        List<String> testData = IntStream.range(0, 999).mapToObj(Integer::toString).collect(Collectors.toList());
        List<String> expected = testData.stream().map(i -> "Processed" + i).collect(Collectors.toList());
        CountDownLatch testChannelLatch = new CountDownLatch(testData.size());
        KafkaSampleBean kafkaConsumingBean = cdiContainer.select(KafkaSampleBean.class).get();
        kafkaConsumingBean.setCountDownLatch(testChannelLatch);
        produceAndCheck(kafkaConsumingBean, testData, TEST_TOPIC_2, testChannelLatch, expected);
    }

    @Test
    void error() {
        LOGGER.fine("==========> test error()");
        KafkaSampleBean kafkaConsumingBean = cdiContainer.select(KafkaSampleBean.class).get();
        // This is correctly processed
        List<String> testData = Collections.singletonList("1");
        CountDownLatch testChannelLatch = new CountDownLatch(testData.size());
        kafkaConsumingBean.setCountDownLatch(testChannelLatch);
        produceAndCheck(kafkaConsumingBean, testData, TEST_TOPIC_3, testChannelLatch, testData);
        // This will throw a run time error in KafkaSampleBean#error
        testData = Collections.singletonList("error");
        testChannelLatch = new CountDownLatch(testData.size());
        kafkaConsumingBean.setCountDownLatch(testChannelLatch);
        produceAndCheck(kafkaConsumingBean, testData, TEST_TOPIC_3, testChannelLatch, Collections.singletonList("1"));
        // After an error, it cannot receive new data
        testData = Collections.singletonList("2");
        testChannelLatch = new CountDownLatch(0);
        kafkaConsumingBean.setCountDownLatch(testChannelLatch);
        // The expected result is not very relevant because there is no waiting time
        produceAndCheck(kafkaConsumingBean, testData, TEST_TOPIC_3, testChannelLatch, Collections.singletonList("1"));
        // But other channels are working, and previous message is not in the list
        testData = Collections.singletonList("3");
        testChannelLatch = new CountDownLatch(testData.size());
        kafkaConsumingBean.setCountDownLatch(testChannelLatch);
        produceAndCheck(kafkaConsumingBean, testData, TEST_TOPIC_1, testChannelLatch, Arrays.asList("1", "3"));
    }

    @Test
    void withBackPressure() {
        LOGGER.fine("==========> test withBackPressure()");
        List<String> testData = IntStream.range(0, 999).mapToObj(i -> "1").collect(Collectors.toList());
        List<String> expected = Arrays.asList("1", "1", "1");
        CountDownLatch testChannelLatch = new CountDownLatch(expected.size());
        /*
         * We use different bean because this test doesn't ACK everything.
         * Kafka will push again uncommit records and it will make a mess in the tests.
         */
        KafkaNoFullAck1Bean kafkaConsumingBean = cdiContainer.select(KafkaNoFullAck1Bean.class).get();
        kafkaConsumingBean.setCountDownLatch(testChannelLatch);
        produceAndCheck(kafkaConsumingBean, testData, TEST_TOPIC_4, testChannelLatch, expected);
    }

    @Test
    void withBackPressureAndError() {
        LOGGER.fine("==========> test withBackPressureAndError()");
        List<String> testData = Arrays.asList("2", "2");
        CountDownLatch testChannelLatch = new CountDownLatch(testData.size());
        /*
         * We use different bean because this test doesn't ACK everything.
         * Kafka will push again uncommit records and it will make a mess in the tests.
         */
        KafkaNoFullAck2Bean kafkaConsumingBean = cdiContainer.select(KafkaNoFullAck2Bean.class).get();
        kafkaConsumingBean.setCountDownLatch(testChannelLatch);
        produceAndCheck(kafkaConsumingBean, testData, TEST_TOPIC_5, testChannelLatch, testData);
        testData = Collections.singletonList("not a number");
        testChannelLatch = new CountDownLatch(testData.size());
        kafkaConsumingBean.setCountDownLatch(testChannelLatch);
        produceAndCheck(kafkaConsumingBean, testData, TEST_TOPIC_5, testChannelLatch, Arrays.asList("2", "2", "error"));
    }

    @Test
    public void someEventsNoAck2() throws InterruptedException {
        testAckLeftOvers(
                Arrays.asList("1", KafkaSampleBean.NO_ACK, "bad luck", "2","3"),    // First run test data
                1,                                                                  // Expected commits in first run
                Arrays.asList("4"),                                                 // Second run test data
                Arrays.asList("4", KafkaSampleBean.NO_ACK, "bad luck", "2","3")     // Expected received data in second run
        );
        testAckLeftOvers(
                Arrays.asList("1", "2","3", KafkaSampleBean.NO_ACK, "bad luck"),
                3,
                Arrays.asList("4"),
                Arrays.asList("4", KafkaSampleBean.NO_ACK, "bad luck")
        );
        testAckLeftOvers(
                Arrays.asList(KafkaSampleBean.NO_ACK, "bad luck", "2","3"),
                0,
                Arrays.asList("4"),
                Arrays.asList("4", KafkaSampleBean.NO_ACK, "bad luck", "2","3")
        );
        testAckLeftOvers(
                Arrays.asList("a", "b", "c", "d", KafkaSampleBean.NO_ACK, "bad luck", "2","3"),
                4,
                Arrays.asList(),
                Arrays.asList(KafkaSampleBean.NO_ACK, "bad luck", "2","3")
        );
        testAckLeftOvers(
                Arrays.asList(KafkaSampleBean.NO_ACK),
                0,
                Arrays.asList(),
                Arrays.asList(KafkaSampleBean.NO_ACK)
        );
        testAckLeftOvers(
                Arrays.asList("a", "b", "c", "d"),
                4,
                Arrays.asList("x"),
                Arrays.asList("x")
        );
    }

    private void produceAndCheck(AbstractSampleBean kafkaConsumingBean, List<String> testData, String topic,
                                 CountDownLatch testChannelLatch, List<String> expected) {
        Map<String, Object> config = new HashMap<>();
        config.put("bootstrap.servers", kafkaResource.getKafkaConnectString());
        config.put("key.serializer", LongSerializer.class.getName());
        config.put("value.serializer", StringSerializer.class.getName());
        try (BasicKafkaProducer<Long, String> producer =
                     new BasicKafkaProducer<>(Collections.singletonList(topic), new KafkaProducer<>(config))) {
            LOGGER.fine("Producing " + testData.size() + " events");
            //Send all test messages(async send means order is not guaranteed) and in parallel
            testData.forEach(producer::produceAsync);
            // Wait till records are delivered
            boolean consumed = false;
            try {
                consumed = testChannelLatch.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LOGGER.fine("Time out");
            }
            assertTrue(consumed, "All expected messages were not consumed. Missing: " + testChannelLatch.getCount());
            Collections.sort(kafkaConsumingBean.consumed());
            Collections.sort(expected);
            assertEquals(expected, kafkaConsumingBean.consumed());
        }
    }

    private void testAckLeftOvers(List<String> firstRunTestData,
                                  int expectedCommitsFirstRun,
                                  List<String> secondRunTestData,
                                  List<String> secondRunExpected) throws InterruptedException {
        CountDownLatch testChannelLatch = new CountDownLatch(firstRunTestData.size());
        KafkaSampleBean kafkaConsumingBean = cdiContainer.select(KafkaSampleBean.class).get();
        kafkaConsumingBean.setCountDownLatch(testChannelLatch);
        kafkaConsumingBean.setCommitLatch(new CountDownLatch(expectedCommitsFirstRun));
        //Same data in and out
        produceAndCheck(kafkaConsumingBean, firstRunTestData, TEST_TOPIC_6, testChannelLatch, firstRunTestData);
        assertTrue(kafkaConsumingBean.getCommitLatch().await(5, TimeUnit.SECONDS),
                "Less than expected confirmed acks");
        // We need to restart, so the connection to Kafka is restarted too. Then it can push again uncommitted messages
        LOGGER.fine("Restarting");
        tearDown();
        setUp();
        testChannelLatch = new CountDownLatch(secondRunExpected.size());
        kafkaConsumingBean = cdiContainer.select(KafkaSampleBean.class).get();
        kafkaConsumingBean.setCountDownLatch(testChannelLatch);
        kafkaConsumingBean.setCommitLatch(new CountDownLatch(secondRunExpected.size()));
        kafkaConsumingBean.commitEveryThing();
        // Expect leftovers from first run
        produceAndCheck(kafkaConsumingBean, secondRunTestData, TEST_TOPIC_6, testChannelLatch, secondRunExpected);
        LOGGER.fine("Restarting");
        tearDown();
        setUp();
    }

    private <T> Instance<T> getInstance(Class<T> beanType, Annotation annotation) {
        return cdiContainer.select(beanType, annotation);
    }

    private SeContainer startCdiContainer(Map<String, String> p, Set<Class<?>> beanClasses) {
        p.put("mp.initializer.allow", "true");
        Config config = Config.builder()
                .sources(ConfigSources.create(p))
                .build();
        MpConfigProviderResolver.instance()
                .registerConfig((org.eclipse.microprofile.config.Config) config,
                        Thread.currentThread().getContextClassLoader());
        final SeContainerInitializer initializer = SeContainerInitializer.newInstance();
        assertNotNull(initializer);
        initializer.addBeanClasses(beanClasses.toArray(new Class<?>[0]));
        return initializer.initialize();
    }
}