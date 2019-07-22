package com.cloudera

import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, HasOffsetRanges, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.{Jedis, Pipeline}

object PassengerFlowConsumerRedis {
  private val logger: Logger = Logger.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    val properties = PropertiesScalaUtils.loadProperties("passenger_flow.properties")
    val kafkaParams = Map[String, Object](
      ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG -> properties.getProperty("kafka1.bootstrap.servers"),
      ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG -> classOf[StringDeserializer],
      ConsumerConfig.GROUP_ID_CONFIG -> "group-03",
      ConsumerConfig.AUTO_OFFSET_RESET_CONFIG -> properties.getProperty("kafka1.auto.offset.reset"),
      ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG -> (false: java.lang.Boolean))

    val topic: String = properties.getProperty("kafka1.topic")


    val conf = new SparkConf().setIfMissing("spark.master", "local[2]").setAppName("UserClickCountStat")

    val streamingContext = new StreamingContext(conf, Seconds(5))


    val redisHost = properties.getProperty("redis.host")
    val redisPort = properties.getProperty("redis.port")
    val redisTimeout = properties.getProperty("redis.timeout")
    val maxTotal = properties.getProperty("redis.maxTotal")
    val maxIdle = properties.getProperty("redis.maxIdle")
    val minIdle = properties.getProperty("redis.minIdle")

    JedisPoolUtils.makePool(redisHost, redisPort.toInt, redisTimeout.toInt, maxTotal.toInt, maxIdle.toInt, minIdle.toInt)

    val jedis: Jedis = JedisPoolUtils.getPool.getResource

    val partition = 2

    val fromOffsets: Map[TopicPartition, Long] = readOffsets(jedis, topic, partition)
    val kafkaStream: InputDStream[ConsumerRecord[String, String]] = KafkaUtils.createDirectStream(
      streamingContext,
      LocationStrategies.PreferConsistent,
      ConsumerStrategies.Assign[String, String](fromOffsets.keys.toList, kafkaParams, fromOffsets)
    )


    //开始处理批次消息
    kafkaStream.foreachRDD(rdd => {
      //获取当前批次的RDD的偏移量
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

      // 获取kafka中的数据并解析成MyRecord
      val result = processLogs(rdd)

      val jedisClient = JedisPoolUtils.getPool.getResource
      val pipline: Pipeline = jedisClient.pipelined();

      //开启事务
      pipline.multi()
      result.foreach({
        println(_)
      })
      println("=============== Total " + result.length + " events in this    batch ..")
      //逐条处理消息
      var values: RDD[(String, Int)] = rdd.map(_.value()).flatMap(parseLog).filter(_ != null).distinct().map(record => {
        (record.deviceId, record.inNum)
      }).reduceByKey(_ + _)

      values.foreach(println(_))

      //更新Offset
      offsetRanges.foreach { offsetRange =>
        println("partition : " + offsetRange.partition + " fromOffset:  " + offsetRange.fromOffset + " untilOffset: " + offsetRange.untilOffset)
        val topic_partition_key = offsetRange.topic + "_" + offsetRange.partition
        pipline.set(topic_partition_key, offsetRange.untilOffset.toString)
      }

      //提交事务
      pipline.exec(); //关闭pipeline
      pipline.sync(); //关闭连接
      jedisClient.close()
    })

    streamingContext.start()
    streamingContext.awaitTermination()
    streamingContext.stop()

    case class MyRecord(deviceId: String, cmdType: String, parentDeviceId: String, startTime: String, totalTime: Int, inNum: Int, outNum: Int) extends Serializable

    def processLogs(rdd: RDD[ConsumerRecord[String, String]]): Array[MyRecord] = {
      rdd.map(_.value()).flatMap(parseLog).filter(_ != null).distinct().collect()
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
        case ex: Exception => println(ex.getMessage) //        throw ex
      }
      return None
    }

  }

  def readOffsets(jedis: Jedis, topic: String, partition: Int): Map[TopicPartition, Long] = {
    //设置每个分区起始的Offset
    var fromOffsets: Map[TopicPartition, Long] = Map()

    for (i <- 0 until partition) {
      val topic_partition_key = topic + "_" + i

      if (!jedis.exists(topic_partition_key)) {
        jedis.set(topic_partition_key, "0")
      }
      val lastSavedOffset = jedis.get(topic_partition_key)

      if (null != lastSavedOffset) {
        try {
          fromOffsets += ((new TopicPartition(topic, i) -> lastSavedOffset.toLong))
        } catch {
          case ex: Exception => println(ex.getMessage)
            println("get lastSavedOffset error, lastSavedOffset from redis [" + lastSavedOffset + "] ")
            System.exit(1)
        }
      }
    }
    jedis.close()
    fromOffsets
  }

}