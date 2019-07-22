package com.cloudera.passengerflow

import org.apache.log4j.Logger
import org.apache.spark.broadcast.Broadcast
import redis.clients.jedis.Pipeline

object LogicData {

  private val logger: Logger = Logger.getLogger(this.getClass)

  private val passflowNamespace = ConfigUtils.getProperty("redis.namespace.passengerflow")
  private val bigscreenNamespace = ConfigUtils.getProperty("redis.namespace.bigscreen")


  /**
    * 大屏统计
    *
    * @param pipeline
    * @param flag
    * @param record
    * @param tuple
    */
  def bigScreen(pipeline: Pipeline, record: PassengerFlowRecord, broadcast: Broadcast[Array[(String, String, String)]]): Unit = {

    val year = record.startTime.substring(0, 4)
    val month = record.startTime.substring(4, 6)
    val day = record.startTime.substring(6, 8)
    val hours = record.startTime.substring(8, 10)
    val minutes = record.startTime.substring(10, 12).toInt
    val seconds = record.startTime.substring(12, 14).toInt
    broadcast.value.foreach(tuple => {
      if (tuple._1 == record.deviceId) {
        if (tuple._3 == "1") {
          //每个组织按小时统计 bigscreen:年月日:组织ID  半小时  count
          if (minutes < 30 && seconds <= 59) {
            //各组织进
            pipeline.hincrBy(s"${bigscreenNamespace}:${year}:${month}:${day}:in:${tuple._2}", hours + "30", record.inNum)
            //各组织出
            pipeline.hincrBy(s"${bigscreenNamespace}:${year}:${month}:${day}:out:${tuple._2}", hours + "30", record.outNum)
          } else {
            //各组织进
            pipeline.hincrBy(s"${bigscreenNamespace}:${year}:${month}:${day}:in:${tuple._2}", "%02d".format(hours.toInt + 1)  + "00", record.inNum)
            //各组织出
            pipeline.hincrBy(s"${bigscreenNamespace}:${year}:${month}:${day}:out:${tuple._2}", "%02d".format(hours.toInt + 1) + "00", record.outNum)
          }
          //每个组织按天统计  bigscreen:年月:组织ID  日  count
          pipeline.hincrBy(s"${bigscreenNamespace}:${year}:${month}:in:${tuple._2}", day, record.inNum)
          pipeline.hincrBy(s"${bigscreenNamespace}:${year}:${month}:out:${tuple._2}", day, record.outNum)
          //每个组织按月统计 bigscreen:年:组织ID  月  count
          pipeline.hincrBy(s"${bigscreenNamespace}:${year}:in:${tuple._2}", month, record.inNum)
          pipeline.hincrBy(s"${bigscreenNamespace}:${year}:out:${tuple._2}", month, record.outNum)
          //        //每个组织按年统计
          //        pipeline.incrBy(s"${bigscreenNamespace}:${year}:${tuple._2}_in", record.inNum)
          //        pipeline.incrBy(s"${bigscreenNamespace}:${year}:${tuple._2}_out", record.outNum)

        } else if (tuple._3 == "2") {
          //每个组织按小时统计 bigscreen:年月日:组织ID  半小时  count
          if (minutes < 30 && seconds <= 59) {
            //各组织进
            pipeline.hincrBy(s"${bigscreenNamespace}:${year}:${month}:${day}:in:${tuple._2}", hours + "30", record.outNum)
            //各组织出
            pipeline.hincrBy(s"${bigscreenNamespace}:${year}:${month}:${day}:out:${tuple._2}", hours + "30", record.inNum)
          } else {
            //各组织进
            pipeline.hincrBy(s"${bigscreenNamespace}:${year}:${month}:${day}:in:${tuple._2}", (hours + 1)  + "00", record.outNum)
            //各组织出
            pipeline.hincrBy(s"${bigscreenNamespace}:${year}:${month}:${day}:out${tuple._2}", (hours + 1) + "00", record.inNum)
          }
          //每个组织按天统计  bigscreen:年月:组织ID  日  count
          pipeline.hincrBy(s"${bigscreenNamespace}:${year}:${month}:in:${tuple._2}", day, record.outNum)
          pipeline.hincrBy(s"${bigscreenNamespace}:${year}:${month}:out:${tuple._2}", day, record.inNum)
          //每个组织按月统计 bigscreen:年:组织ID  月  count
          pipeline.hincrBy(s"${bigscreenNamespace}:${year}:in:${tuple._2}", month, record.outNum)
          pipeline.hincrBy(s"${bigscreenNamespace}:${year}:out:${tuple._2}", month, record.inNum)
          //        //每个组织按年统计
          //        pipeline.incrBy(s"${bigscreenNamespace}:${year}:${tuple._2}_in", record.inNum)
          //        pipeline.incrBy(s"${bigscreenNamespace}:${year}:${tuple._2}_out", record.outNum)
        } else {
          logger.info(s"组织与设备绑定正反向关系配置有误,组织id为${tuple._2},设备id为${tuple._1},绑定关系为${tuple._3}")
        }
      }
    })
  }

  /**
    * 客流统计后台
    *
    * @param pipeline
    * @param flag
    * @param record
    * @param tuple
    */
  def passengerflow(pipeline: Pipeline, record: PassengerFlowRecord, broadcast: Broadcast[Array[(String, String, String)]]): Unit = {
    broadcast.value.foreach(tuple => {
      if (tuple._1 == record.deviceId) {
//        println(s"=== ${tuple._2} ====== ${record.deviceId} ===== ${record.inNum} ===== ${record.outNum}")
        pipeline.hincrBy(s"${passflowNamespace}:${record.startTime.substring(0, 8)}:${tuple._2}", record.startTime.substring(8, 12), record.inNum + record.outNum)
        pipeline.expire(s"${passflowNamespace}:${record.startTime.substring(0, 8)}:${tuple._2}", 60 * 60 * 24 * 7)
      }
    })
  }
}
