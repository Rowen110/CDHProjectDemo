package com.cloudera

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.Logger
import org.apache.spark
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.DStream
import org.apache.spark.{SparkConf, rdd}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}

object RemoteSubmitApp {
  val logger = Logger.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {
    // 设置提交任务的用户
    //    System.setProperty("HADOOP_USER_NAME", "root")
    val conf = new SparkConf().setAppName("Remote_Submit_App")
      // 设置yarn-client模式提交
      .setMaster("yarn-client") // 设置resourcemanager的ip
      .set("yarn.resourcemanager.hostname", "bigdata-dev-42")
      // 设置driver的内存大小
      .set("spark.driver.memory", "1024M")
      // 设置executor的内存大小
      .set("spark.executor.memory", "800M")
      // 设置executor的个数
      .set("spark.executor.instance", "2")
      // 设置提交任务的 yarn 队列
      //      .set("spark.yarn.queue", "defalut")
      // 设置driver的 ip 地址,即本机的 ip 地址
      .set("spark.driver.host", "172.25.86.225")
//      .set("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      // 设置jar包的路径,如果有其他的依赖包,可以在这里添加,逗号隔开
      .setJars(List("E:\\RemoteSubmitSparkToYarn\\target\\RemoteSubmitSparkToYarn-1.0-SNAPSHOT.jar"))

    val scc = new StreamingContext(conf, Seconds(30))

    scc.sparkContext.setLogLevel("WARN")
//    scc.checkpoint("checkpoint")
    val topic = "remote_submit_test"
    val topicSet = topic.split(",").toSet

    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "10.101.75.190:9092,10.101.75.191:9092,10.101.75.192:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> "remote_test",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )

    val kafkaStreams = KafkaUtils.createDirectStream[String, String](
      scc,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topicSet, kafkaParams)
    )

    val wordCounts: DStream[(String, Long)] = kafkaStreams.map(_.value())
      .flatMap(_.split(" "))
      .map(x => (x, 1L))
      .reduceByKey(_ + _)
    wordCounts.print()


    //启动流
    scc.start()
    scc.awaitTermination()
  }
}
