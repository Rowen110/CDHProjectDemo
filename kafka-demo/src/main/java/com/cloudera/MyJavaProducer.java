package com.cloudera;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Date;
import java.util.Properties;
import java.util.UUID;

/**
 * @author Charles
 * @package com.cloudera
 * @classname Myproducer
 * @description TODO
 * @date 2019-6-18 15:53
 */
public class MyJavaProducer {

    public static String TOPIC_NAME = "test";

    public static void main(String[] args) {

        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "bigdata-dev-43:9092,slave2:9092,slave1:9092");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, "0");
        props.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384"); //批量发送的字节数
        props.put(ProducerConfig.LINGER_MS_CONFIG, "1"); //将会减少请求数目，但是同时会增加1ms的延迟
        props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "33554432"); //用来缓存数据的内存大小
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");


        Producer<String, String> producer = new KafkaProducer<String, String>(props);

        for (int i = 0; i < 10; i++) {

            String key = "Key-" + i;
            long runtime = new Date().getTime();
            String message = "Message-" + i + ":" + runtime + "," + UUID.randomUUID();

            ProducerRecord record = new ProducerRecord<String, String>(TOPIC_NAME, key, message);
            producer.send(record);
            System.out.println(key + "----" + message);
        }

        producer.flush();
        producer.close();
    }
}

