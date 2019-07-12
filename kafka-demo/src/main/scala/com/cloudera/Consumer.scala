package com.hdjt.bigdata.passengerFlow

import com.cloudera.KafkaZkUtils
import kafka.utils.ZkUtils
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.kafka010.HasOffsetRanges
import org.apache.spark.streaming.{Seconds, StreamingContext}


object Consumer {

  private val logger: Logger = Logger.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setMaster("local[2]").setAppName("UserClickCountStat")

    val streamingContext = new StreamingContext(conf, Seconds(5))


    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> "bigdata-dev-43:9092,slave2:9092,slave1:9092",
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> "group-01",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> "earliest",
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean)
    )

    val topic: String = "test-test1"

    val zkUrl = "10.101.71.41:2181"
    val sessionTimeout = 1000
    val connectionTimeout = 1000

    val zkClient = ZkUtils.createZkClient(zkUrl, sessionTimeout, connectionTimeout)

    val kafkaStream = KafkaZkUtils.createDirectStream(zkClient, streamingContext, kafkaParams, topic)


    //开始处理批次消息
    kafkaStream.foreachRDD(rdd => {
      //获取当前批次的RDD的偏移量
      val offsetsList = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

      // 获取kafka中的数据并解析成MyRecord
      val result = processLogs(rdd)
//      result.map().re
      println("=============== Total " + result.length + " events in this    batch ..")

      //      val results = yourCalculation(rdd)
      //更新offset到zookeeper中
      KafkaZkUtils.saveOffsets(zkClient, topic, KafkaZkUtils.getZkPath(kafkaParams, topic), rdd)
    })


    streamingContext.start()
    streamingContext.awaitTermination()
    streamingContext.stop()
  }


  case class MyRecord(deviceId: String, cmdType: String, parentDeviceId: String, startTime: String, totalTime: Int, inNum: Int, outNum: Int)

  def processLogs(messages: RDD[ConsumerRecord[String, String]]): Array[MyRecord] = {
    messages.map(_.value()).flatMap(parseLog).distinct().collect()
  }

  //解析每条数据，生成MyRecord
  def parseLog(line: String): Option[MyRecord] = {
    val ary: Array[String] = line.split("\\|", -1);
    try {

      val deviceId = ary(0)
      val cmdType = ary(1)
      val parentDeviceId = ary(2)
      val startTime = ary(3)
      val totalTime = ary(4).toInt
      val inNum = ary(5).toInt
      val outNum = ary(6).toInt

      return Some(MyRecord(deviceId, cmdType, parentDeviceId, startTime, totalTime, inNum, outNum))

    } catch {
      case ex: Exception => println(ex.getMessage)
    }
    return None
  }
}