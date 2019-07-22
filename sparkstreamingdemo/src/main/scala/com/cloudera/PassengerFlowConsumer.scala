package com.cloudera

import java.io.File

import com.cloudera.common.KafkaSink
import com.cloudera.utils.{JedisPoolUtils, KafkaZkUtils}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.log4j.Logger
import org.apache.spark.SparkConf
import org.apache.spark.broadcast.Broadcast
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.kafka010.HasOffsetRanges
import org.apache.spark.streaming.{Seconds, StreamingContext}
import redis.clients.jedis.Pipeline

import scala.collection.immutable.Map


object PassengerFlowConsumer {
  private val logger: Logger = Logger.getLogger(this.getClass)

  def main(args: Array[String]): Unit = {

    val topic: String = ConfigUtils.getProperty("kafka.input.topic")

    val kafkaParams = ConfigUtils.getKafkaConsumerParams()

    val zkClient = ConfigUtils.getZkClient()

    JedisPoolUtils.makePool(ConfigUtils.getRedisConfig)

    System.setProperty("HADOOP_USER_NAME", "hive")
    val warehouseLocation = new File(ConfigUtils.getProperty("hive.warehouse.dir")).getAbsolutePath

    val conf = new SparkConf()
      .setIfMissing("spark.master", "local[2]")
      //开启背压
      .set("spark.streaming.backpressure.enabled","true")
      //设置背压
      .set("spark.streaming.kafka.maxRatePerPartition","5000")
      .setAppName("passengerFlowCount")
    val spark: SparkSession = SparkSession.builder().config(conf)
      .config("spark.sql.warehouse.dir", warehouseLocation)
      .enableHiveSupport()
      .getOrCreate()

    @transient
    val sc = spark.sparkContext
    val streamingContext = new StreamingContext(sc, Seconds(5))

    val kafkaStream = KafkaZkUtils.createDirectStream(zkClient, streamingContext, kafkaParams, topic)

    // 初始化KafkaSink,并广播
    val kafkaProducer: Broadcast[KafkaSink[String, String]] = {
      val kafkaProducerConfig: Map[String, Object] = ConfigUtils.getKafkaProducerParams()
      if (logger.isInfoEnabled) {
        logger.info("kafka producer init done!")
      }
      streamingContext.sparkContext.broadcast(KafkaSink[String, String](kafkaProducerConfig))
    }

    //开始处理批次消息
    kafkaStream.foreachRDD(rdd => {
      //获取当前批次的RDD的偏移量
      val offsetRanges = rdd.asInstanceOf[HasOffsetRanges].offsetRanges

      if (!rdd.isEmpty()) {
        val jedisClient = JedisPoolUtils.getPool.getResource

        val profileTuples: Array[(String, String, String)] = ConfigUtils.getFlowproFile(jedisClient,spark)
        // 判断客流配置是否为空
        if (!profileTuples.isEmpty) {

          // 广播组织配置信息
          val broadcast: Broadcast[Array[(String, String, String)]] = streamingContext.sparkContext.broadcast(profileTuples)

          // 获取kafka中的数据并解析成MyRecord
          val result = processLogs(rdd)

          result.foreach(println(_))
          logger.info("=============== Total " + result.length + " events in this    batch ..")

          val pipeline: Pipeline = jedisClient.pipelined()

          //开启事务
          pipeline.multi() //逐条处理消息
          try {
            result.foreach(record => {
              //  客流统计后台逻辑
              LogicData.passengerflow(pipeline, record, broadcast)
              //大屏数据逻辑
              LogicData.bigScreen(pipeline, record, broadcast)
            })
            //提交事务
            pipeline.exec()
            //关闭pipeline
            pipeline.sync()
            // 读取Redis中大屏相关指标统计结果
            val bigScreenJson: String = BigScreenStat.getBigScreenStatResult(jedisClient);
            // 将统计数据发送到Kafka
            kafkaProducer.value.send(ConfigUtils.getProperty("Kafka.output.topic"), ConfigUtils.getProperty("kafka.bigscreen.passengerflow.key"), bigScreenJson);
            //更新offset到zookeeper中
            KafkaZkUtils.saveOffsets(zkClient,topic,KafkaZkUtils.getZkPath(kafkaParams,topic),rdd)
          } catch {
            case e: Exception => {
              logger.error("客流统计计算异常", e)
              pipeline.discard()
            }
          } finally {
            //关闭连接
            pipeline.close()
            jedisClient.close()
            //销毁广播变量
            broadcast.destroy()
          }
        }else{
          jedisClient.close()
        }
      }
    })
    streamingContext.start()
    streamingContext.awaitTermination()
    streamingContext.stop()
  }


  def processLogs(rdd: RDD[ConsumerRecord[String, String]]): Array[PassengerFlowRecord] = {
    rdd.map(_.value()).flatMap(parseLog).filter(_ != null).distinct().collect()
  }

  //解析每条数据，生成MyRecord
  def parseLog(line: String): Option[PassengerFlowRecord] = {
    val ary: Array[String] = line.split("\\|", -1);
    try {
      val deviceId = ary(0).trim
      val cmdType = ary(1).trim
      val parentDeviceId = ary(2).trim
      val startTime = ary(3).trim.replace("-", "").replace(":", "").replace(" ", "")
      val totalTime = ary(4).trim.toInt
      val inNum = ary(5).trim.toInt
      val outNum = ary(6).trim.toInt

      return Some(PassengerFlowRecord(deviceId, cmdType, parentDeviceId, startTime, totalTime, inNum, outNum))
    } catch {
      case ex: Exception => logger.debug(ex.getMessage)
    }
    return None
  }
}