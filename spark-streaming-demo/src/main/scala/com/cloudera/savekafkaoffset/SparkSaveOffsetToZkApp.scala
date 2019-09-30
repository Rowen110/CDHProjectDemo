package com.cloudera.savekafkaoffset

import com.cloudera.utils.KafkaZkUtils
import kafka.utils.ZkUtils
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.streaming.kafka010.HasOffsetRanges
import org.apache.spark.streaming.{Seconds, StreamingContext}

object SparkSaveOffsetToZkApp {

  private val logger: Logger = Logger.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setIfMissing("spark.master", "local[2]").setAppName("Spark Save Offset To Zookeeper App")

    val streamingContext = new StreamingContext(conf, Seconds(30))

    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "10.101.75.190:9092,10.101.75.191:9092,10.101.75.192:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> "group-01",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )

    val topic: String = "my-topic"
    val zkUrl = "10.101.71.41:2181,10.101.71.42:2181,10.101.71.43:2181"
    val sessionTimeout = 1000
    val connectionTimeout = 1000

    val zkClient = ZkUtils.createZkClient(zkUrl, sessionTimeout, connectionTimeout)

    val kafkaStream = KafkaZkUtils.createDirectStream(zkClient, streamingContext, kafkaParams, topic)

    //开始处理批次消息
    kafkaStream.foreachRDD(rdd => {
      //获取当前批次的RDD的偏移量
      val offsetsList = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

      // 处理从获取 kafka 中的数据
      val result = rdd.map(_.value()).map(_.split("\\|\\|")).map(x => (x(0), x(1), x(2)))
      result.foreach(println(_))
      println("=============== Total " + rdd.count() + " events in this    batch ..")

      // 更新offset到zookeeper中
      KafkaZkUtils.saveOffsets(zkClient, topic, KafkaZkUtils.getZkPath(kafkaParams, topic), rdd)
    })
    streamingContext.start()
    streamingContext.awaitTermination()
    streamingContext.stop()
  }
}