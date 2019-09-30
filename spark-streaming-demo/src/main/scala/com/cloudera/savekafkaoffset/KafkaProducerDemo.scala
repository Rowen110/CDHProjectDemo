package com.cloudera.savekafkaoffset

import java.text.SimpleDateFormat
import java.util.concurrent.Future
import java.util.{Date, Properties, UUID}

import org.apache.kafka.clients.producer.{KafkaProducer, ProducerConfig, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.serialization.StringSerializer

import scala.util.Random

object KafkaProducerDemo {

  def main(args: Array[String]): Unit = {

    // 设置 Kafka 配置属性
    val props = new Properties
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "10.101.75.190:9092,10.101.75.191:9092,10.101.75.192:9092")
    props.put(ProducerConfig.ACKS_CONFIG, "all")
    props.put(ProducerConfig.RETRIES_CONFIG, "0")
    props.put(ProducerConfig.BATCH_SIZE_CONFIG, "16384") //批量发送的字节数
    props.put(ProducerConfig.LINGER_MS_CONFIG, "1") //将会减少请求数目，但是同时会增加1ms的延迟
    props.put(ProducerConfig.BUFFER_MEMORY_CONFIG, "33554432") //用来缓存数据的内存大小
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, classOf[StringSerializer].getName)

    val producer = new KafkaProducer[String, String](props)

    val TOPIC_NAME = "my-topic"

    try {
      // 产生并发送消息
      while (true) {
        val runtime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date().getTime)

        // 2019-09-30 08:46:10||24016FFD664A4C32AE3A4276416329B0||6
        val message: String = s"${runtime}||${UUID.randomUUID().toString.replace("-", "").toUpperCase}||${Random.nextInt()}"

        val record: ProducerRecord[String, String] = new ProducerRecord[String, String](TOPIC_NAME, message)
        // 发送消息，并获得一个Future对象
        val metadataFuture: Future[RecordMetadata] = producer.send(record)
        //同步获得Future对象的结果
        val recordMetadata: RecordMetadata = metadataFuture.get()

        Thread.sleep(500)
        println(message)
      }
    } catch {
      case e: Exception => {
        //要考虑重试
        e.printStackTrace();
      }
    }
    // 关闭producer
    producer.flush()
    producer.close()
  }
}
