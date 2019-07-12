package com.cloudera;


import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;

import java.util.*;


public class ReceiveLatestMessageMain {
    private static final int COUNT = 5;

    public static void main(String... args) throws Exception {
        Properties props = new Properties();

        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "bigdata-dev-43:9092,slave2:9092,slave1:9092");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "yq-consumer12");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);

//        1. 减少 max.poll.records
//        2. 增加 session.timeout.ms
//        3. 减少 auto.commit.interval.ms
        props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 500);
        props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");


        System.out.println("create KafkaConsumer");
        final KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        AdminClient adminClient = AdminClient.create(props);
        String topic = "test-test1";

        adminClient.describeTopics(Arrays.asList(topic));

        try {
            DescribeTopicsResult topicResult = adminClient.describeTopics(Arrays.asList(topic));
            Map<String, KafkaFuture<TopicDescription>> descMap = topicResult.values();
            Iterator<Map.Entry<String, KafkaFuture<TopicDescription>>> itr = descMap.entrySet().iterator();
            while (itr.hasNext()) {
                Map.Entry<String, KafkaFuture<TopicDescription>> entry = itr.next();
                System.out.println("key: " + entry.getKey());
                List<TopicPartitionInfo> topicPartitionInfoList = entry.getValue().get().partitions();
                topicPartitionInfoList.forEach((e) -> {
                    int partitionId = e.partition();
                    Node node = e.leader();
                    TopicPartition topicPartition = new TopicPartition(topic, partitionId);
                    Map<TopicPartition, Long> mapBeginning = consumer.beginningOffsets(Arrays.asList(topicPartition));
                    Iterator<Map.Entry<TopicPartition, Long>> itr2 = mapBeginning.entrySet().iterator();
                    long beginOffset = 0;
                    //mapBeginning只有一个元素，因为Arrays.asList(topicPartition)只有一个topicPartition
                    while (itr2.hasNext()) {
                        Map.Entry<TopicPartition, Long> tmpEntry = itr2.next();
                        beginOffset = tmpEntry.getValue();
                    }
                    Map<TopicPartition, Long> mapEnd = consumer.endOffsets(Arrays.asList(topicPartition));
                    Iterator<Map.Entry<TopicPartition, Long>> itr3 = mapEnd.entrySet().iterator();
                    long lastOffset = 0;
                    while (itr3.hasNext()) {
                        Map.Entry<TopicPartition, Long> tmpEntry2 = itr3.next();
                        lastOffset = tmpEntry2.getValue();
                    }
                    long expectedOffSet = lastOffset - COUNT;
                    expectedOffSet = expectedOffSet > 0 ? expectedOffSet : 1;
                    System.out.println("Leader of partitionId: " + partitionId + "  is " + node + ".  expectedOffSet:" + expectedOffSet
                            + "，  beginOffset:" + beginOffset + ", lastOffset:" + lastOffset);
                    consumer.commitSync(Collections.singletonMap(topicPartition, new OffsetAndMetadata(expectedOffSet)));
                });
            }

            consumer.subscribe(Arrays.asList(topic));
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(100);
                for (ConsumerRecord<String, String> record : records) {
                    System.out.printf("read offset =%d, key=%s , value= %s, partition=%s\n",
                            record.offset(), record.key(), record.value(), record.partition());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("when calling kafka output error." + ex.getMessage());
        } finally {
            adminClient.close();
            consumer.close();
        }
    }
}