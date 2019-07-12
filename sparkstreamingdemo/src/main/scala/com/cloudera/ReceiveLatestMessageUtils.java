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
import java.util.logging.Logger;


public class ReceiveLatestMessageUtils {

    private static final Logger logger = Logger.getLogger(ReceiveLatestMessageUtils.class.getName());


    public static void receiveLatestMessage(Map<String, Object> kafkaParams, String topic, Integer count) {
        logger.info("create KafkaConsumer");

        final KafkaConsumer consumer = new KafkaConsumer<>(kafkaParams);

        AdminClient adminClient = AdminClient.create(kafkaParams);

        try {
            DescribeTopicsResult topicResult = adminClient.describeTopics(Arrays.asList(topic));

            Map<String, KafkaFuture<TopicDescription>> descMap = topicResult.values();

            Iterator<Map.Entry<String, KafkaFuture<TopicDescription>>> itr = descMap.entrySet().iterator();

            while (itr.hasNext()) {
                Map.Entry<String, KafkaFuture<TopicDescription>> entry = itr.next();
                logger.info("key: " + entry.getKey());
                List<TopicPartitionInfo> topicPartitionInfoList = entry.getValue().get().partitions();

                for (TopicPartitionInfo topicPartitionInfo : topicPartitionInfoList) {
                    consumerAction(topicPartitionInfo, consumer, topic, count);
                }
            }
            consumer.subscribe(Arrays.asList(topic));
            while (true) {
                ConsumerRecords<String,String> records = consumer.poll(100);
                for (ConsumerRecord<String,String> record : records) {
                    System.out.printf("read offset =%d, key=%s , value= %s, partition=%s\n",
                            record.offset(), record.key(), record.value(), record.partition());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.info("when calling kafka output error." + ex.getMessage());
        } finally {
            adminClient.close();
            consumer.close();
        }
    }

    private static void consumerAction(TopicPartitionInfo topicPartitionInfo, KafkaConsumer<String, String> consumer, String topic, Integer count) {

        int partitionId = topicPartitionInfo.partition();

        Node node = topicPartitionInfo.leader();

        TopicPartition topicPartition = new TopicPartition(topic, partitionId);

        Map<TopicPartition, Long> mapBeginning = consumer.beginningOffsets(Arrays.asList(topicPartition));

        Iterator<Map.Entry<TopicPartition, Long>> beginIterator = mapBeginning.entrySet().iterator();
        long beginOffset = 0;

        //mapBeginning只有一个元素，因为Arrays.asList(topicPartition)只有一个topicPartition
        while (beginIterator.hasNext()) {
            Map.Entry<TopicPartition, Long> tmpEntry = beginIterator.next();
            beginOffset = tmpEntry.getValue();
        }

        Map<TopicPartition, Long> mapEnd = consumer.endOffsets(Arrays.asList(topicPartition));
        Iterator<Map.Entry<TopicPartition, Long>> endIterator = mapEnd.entrySet().iterator();

        long lastOffset = 0;
        while (endIterator.hasNext()) {
            Map.Entry<TopicPartition, Long> tmpEntry2 = endIterator.next();
            lastOffset = tmpEntry2.getValue();
        }

        long expectedOffSet = lastOffset - count;
        expectedOffSet = expectedOffSet > 0 ? expectedOffSet : 1;
        logger.info("Leader of partitionId: " + partitionId + "  is " + node + ".  expectedOffSet:" + expectedOffSet
                + "，  beginOffset:" + beginOffset + ", lastOffset:" + lastOffset);

        consumer.commitSync(Collections.singletonMap(topicPartition, new OffsetAndMetadata(expectedOffSet)));
    }

    public static void main(String... args) throws Exception {
        Map<String, Object> kafkaParams = new HashMap<String, Object>();
        kafkaParams.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "bigdata-dev-43:9092,slave2:9092,slave1:9092");
        kafkaParams.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer");
        kafkaParams.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        kafkaParams.put(ConsumerConfig.GROUP_ID_CONFIG, "yq-consumer12");
        kafkaParams.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        kafkaParams.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 10);

//        1. 减少 max.poll.records
//        2. 增加 session.timeout.ms
//        3. 减少 auto.commit.interval.ms
        kafkaParams.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 500);
        kafkaParams.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
        kafkaParams.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");

        receiveLatestMessage(kafkaParams, "test-test1", 5);

    }
}
