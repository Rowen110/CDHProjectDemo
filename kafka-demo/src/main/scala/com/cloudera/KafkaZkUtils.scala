package com.cloudera

import kafka.utils.{ZKGroupTopicDirs, ZkUtils}
import org.I0Itec.zkclient.ZkClient
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.log4j.Logger
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.StreamingContext
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, HasOffsetRanges, KafkaUtils}

import scala.collection.immutable.Map

object KafkaZkUtils {
  private val logger: Logger = Logger.getLogger(this.getClass)


  /**
    * 获取 consumer 在zk上的路径
    * @param kafkaParams
    * @param topic
    * @return
    */
  def getZkPath(kafkaParams: Map[String, Object], topic: String): String ={
    val topicDirs = new ZKGroupTopicDirs(kafkaParams.get(ConsumerConfig.GROUP_ID_CONFIG).toString, topic)
    s"${topicDirs.consumerOffsetDir}"
  }

  /**
    * 创建 DirectStream
    * @param zkClient
    * @param streamingContext
    * @param kafkaParams
    * @param topic
    * @return
    */
  def createDirectStream(zkClient: ZkClient,streamingContext: StreamingContext, kafkaParams: Map[String, Object], topic: String): InputDStream[ConsumerRecord[String, String]] = {


    val zkPath = getZkPath(kafkaParams,topic)

    //读取 topic 的 offset
    val storedOffsets = readOffsets(zkClient, topic, zkPath)

    val kafkaStream: InputDStream[ConsumerRecord[String, String]] = storedOffsets match {
      //上次未保存offsets
      case None =>
        KafkaUtils.createDirectStream[String, String](
          streamingContext,
          PreferConsistent,
          ConsumerStrategies.Subscribe[String, String](Array(topic), kafkaParams)
        )
      case Some(fromOffsets) => {
        KafkaUtils.createDirectStream[String, String](
          streamingContext,
          PreferConsistent,
          ConsumerStrategies.Assign[String, String](fromOffsets.keys.toList, kafkaParams, fromOffsets)
        )
      }
    }
    kafkaStream
  }

  /**
    * 保存 offset
    * @param zkClient
    * @param topic
    * @param zkPath
    * @param rdd
    */
  def saveOffsets(zkClient: ZkClient,topic: String, zkPath: String, rdd: RDD[_]): Unit = {

    logger.info("Saving offsets to zookeeper")

    val offsetsRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges
    offsetsRanges.foreach(offsetRange => logger.debug(s"Using ${offsetRange}"))

    val offsetsRangesStr = offsetsRanges.map(offsetRange => s"${offsetRange.partition}:${offsetRange.fromOffset}").mkString(",")

    logger.info(s"Writing offsets to Zookeeper: ${offsetsRangesStr}")

    ZkUtils(zkClient, false).updatePersistentPath(zkPath, offsetsRangesStr)
  }

  /**
    * 读取 offset
    * @param zkClient
    * @param topic
    * @param zkPath
    * @return
    */
  def readOffsets(zkClient: ZkClient, topic: String, zkPath: String): Option[Map[TopicPartition, Long]] = {
    logger.info("Reading offsets from zookeeper")

    val (offsetsRangesStrOpt, _) = ZkUtils(zkClient, false).readDataMaybeNull(zkPath)

    offsetsRangesStrOpt match {
      case Some(offsetsRangesStr) => {
        logger.debug(s"Read offset ranges: ${
          offsetsRangesStr
        }")
        val offsets = offsetsRangesStr.split(",").map(s => s.split(":"))
          .map({
            case Array(partitionStr, offsetStr) =>
              (new TopicPartition(topic, partitionStr.toInt) -> offsetStr.toLong)
          }).toMap
        Some(offsets)
      }
      case None =>
        logger.info("No offsets found in Zookeeper")
        None
    }
  }
}
