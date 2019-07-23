package com.cloudera.passengerflow

import com.cloudera.utils.{JedisPoolUtils, PropertiesScalaUtils}
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.{Level, Logger}
import org.apache.spark.{SparkConf, TaskContext}
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.{Jedis, Pipeline}


object PassengerFlowConsumerRedis {

  private val logger: Logger = Logger.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    Logger.getLogger("org.apache.spark").setLevel(Level.INFO)
    Logger.getLogger("org.eclipse.jetty.server").setLevel(Level.INFO)
    Logger.getLogger("org.apache.kafka.clients.consumer").setLevel(Level.INFO)

    val properties = PropertiesScalaUtils.loadProperties("passenger_flow.properties")
    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> properties.getProperty("kafka.bootstrap.servers"),
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> "group-66",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> properties.getProperty("kafka.auto.offset.reset"),
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean))

    val conf = new SparkConf().setIfMissing("spark.master", "local[4]").setAppName("UserCountStat")

    val streamingContext = new StreamingContext(conf, Seconds(5))


    val redisHost = properties.getProperty("redis.host")
    val redisPort = properties.getProperty("redis.port")
    val redisTimeout = properties.getProperty("redis.timeout")
    val maxTotal = properties.getProperty("redis.maxTotal")
    val maxIdle = properties.getProperty("redis.maxIdle")
    val minIdle = properties.getProperty("redis.minIdle")

    JedisPoolUtils.makePool(redisHost, redisPort.toInt, redisTimeout.toInt, maxTotal.toInt, maxIdle.toInt, minIdle.toInt)

    val jedis: Jedis = JedisPoolUtils.getPool.getResource

    val topicsPartition = Map("topic_1"-> 3,"topic_2"-> 4,"topic_3"-> 5)
    val topics = topicsPartition.keys.toList


    val fromOffsets: Map[TopicPartition, Long] = readOffsets(jedis, topicsPartition)

    val kafkaStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream(
      streamingContext,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Subscribe[String, String](topics, kafkaParams, fromOffsets)
    )


    //开始处理批次消息
    kafkaStream.foreachRDD(rdd => {
      if(!rdd.isEmpty()){
        //获取当前批次的RDD的偏移量
        val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

        rdd.foreachPartition(partition=>{

          val offset: OffsetRange = offsetRanges(TaskContext.get.partitionId)
          logger.info(s"${offset.topic} ${offset.partition} ${offset.fromOffset} ${offset.untilOffset}")

          val jedisClient = JedisPoolUtils.getPool.getResource

          jedisClient.select(1)

          val pipline: Pipeline = jedisClient.pipelined();
          //开启事务
          pipline.multi()
          partition.foreach(record=>{
            //自己的计算逻辑
            println(record)
            println(s"===============${record.topic()}_${record.partition()}====================")
          })
          //更新Offset
          offsetRanges.foreach { offsetRange =>
            logger.info(s"topic: ${offsetRange.topic} === partition: ${offsetRange.partition} === fromOffset: ${offsetRange.fromOffset} === untilOffset: ${offsetRange.untilOffset}")
            val topic_partition_key = offsetRange.topic + "_" + offsetRange.partition
            pipline.set(topic_partition_key, offsetRange.untilOffset.toString)
          }
          //提交事务
          pipline.exec();
          //关闭pipeline
          pipline.sync();
          //关闭连接
          jedisClient.close()
        })
      }
    })
    streamingContext.start()
    streamingContext.awaitTermination()
    streamingContext.stop()

    case class MyRecord(topic: String, id: String, timestramp: String, uuid: String) extends Serializable

    def processLogs(rdd: RDD[ConsumerRecord[String, String]]): Array[MyRecord] = {
      rdd.map(_.value()).flatMap(parseLog).filter(_ != null).distinct().collect()
    }

    //解析每条数据，生成MyRecord
    def parseLog(line: String): Option[MyRecord] = {
      // test3|10|2019-07-22 16:23:55.725|be937ae2-5fc0-49d6-a310-b2cf513bd016
      val ary: Array[String] = line.split("\\|", -1);
      try {
        val topic = ary(0).trim
        val id = ary(1).trim
        val timestramp = ary(2).trim
        val uuid = ary(3).trim

        return Some(MyRecord(topic, id, timestramp, uuid))
      } catch {
        case e: Exception =>
          logger.error("解析错误", e)
          println(e.getMessage)
      }
      return None
    }

  }

  def readOffsets(jedis: Jedis, topicsPartition: Map[String,Int]): Map[TopicPartition, Long] = {
    //设置每个分区起始的Offset
    var fromOffsets: Map[TopicPartition, Long] = Map()
    try {
      jedis.select(1)
      topicsPartition.foreach(topic => {
        var topicFromOffsets: Map[TopicPartition, Long] = Map()
        for (i <- 0 until topic._2.toInt) {
          val topic_partition_key = topic._1 + "_" + i
          if (!jedis.exists(topic_partition_key)) {
            jedis.set(topic_partition_key, "0")
          }
          val lastSavedOffset = jedis.get(topic_partition_key)
          logger.info(s"topic ${topic} partition ${i} get lastSavedOffset from redis: ${lastSavedOffset}")
          topicFromOffsets += (new TopicPartition(topic._1, i) -> lastSavedOffset.toLong)
        }
        fromOffsets ++= topicFromOffsets
      })
    }catch {
      case e: Exception =>
        logger.error("readOffsets error ",e)
        System.exit(1)
    }
    fromOffsets
  }
}
