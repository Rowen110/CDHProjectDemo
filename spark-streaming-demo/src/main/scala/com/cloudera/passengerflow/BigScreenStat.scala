package com.cloudera.passengerflow

import java.util
import java.util.UUID

import com.cloudera.common.{CommonMessage, Header, HeaderConstant}
import com.cloudera.utils.DateUtils
import com.google.gson.Gson
import redis.clients.jedis.Jedis

import scala.collection.JavaConversions._
import scala.collection.immutable.ListMap
import scala.collection.mutable

/**
  * 当日进场人数             PSPC2001
  * 当日在场人数             PSPC2002
  * 当日出场人数             PSPC2003
  * 进一个月的客流量走势      PSPC2004
  * 客流量增长情况           PSPC2005
  * 未来7天的客流量预测      PSPC2006
  */
object BigScreenStat {

  val bigscreen: String = ConfigUtils.getProperty("redis.namespace.bigscreen")

  val DAY_MAP = Map[String, Long]("0000" -> 0, "0030" -> 0, "0100" -> 0, "0130" -> 0, "0200" -> 0, "0230" -> 0, "0300" -> 0, "0330" -> 0, "0400" -> 0, "0430" -> 0, "0500" -> 0, "0530" -> 0, "0600" -> 0, "0630" -> 0, "0700" -> 0, "0730" -> 0, "0800" -> 0, "0830" -> 0, "0900" -> 0, "0930" -> 0, "1000" -> 0, "1030" -> 0, "1100" -> 0, "1130" -> 0, "1200" -> 0, "1230" -> 0, "1300" -> 0, "1330" -> 0, "1400" -> 0, "1430" -> 0, "1500" -> 0, "1530" -> 0, "1600" -> 0, "1630" -> 0, "1700" -> 0, "1730" -> 0, "1800" -> 0, "1830" -> 0, "1900" -> 0, "1930" -> 0, "2000" -> 0, "2030" -> 0, "2100" -> 0, "2130" -> 0, "2200" -> 0, "2230" -> 0, "2300" -> 0, "2330" -> 0, "2400" -> 0)
  val MONTH_MAP = Map[String, Long]("01" -> 0, "02" -> 0, "03" -> 0, "04" -> 0, "05" -> 0, "06" -> 0, "07" -> 0, "08" -> 0, "09" -> 0, "10" -> 0, "11" -> 0, "12" -> 0, "13" -> 0, "14" -> 0, "15" -> 0, "16" -> 0, "17" -> 0, "18" -> 0, "19" -> 0, "20" -> 0, "21" -> 0, "21" -> 0, "22" -> 0, "23" -> 0, "24" -> 0, "25" -> 0, "26" -> 0, "27" -> 0, "28" -> 0, "29" -> 0, "30" -> 0, "31" -> 0)


  /**
    * 获取某月全月累计和该月每天统计结果
    *
    * @param jedis
    * @param month 年 + 月 如：201907
    * @param organizId
    * @return (时间,累计值,总流量统计,进流量统计,出流量统计)
    */
  def getBigScreenStat(jedis: Jedis, organizId: String, date: String): (String, Long, Map[String, Long], Long, Map[String, Long], Long, Map[String, Long]) = {

    val length: Int = date.length
    var inKey, outKey, key: String = ""
    var defaultMap = Map[String, Long]()
    var condition = ""

    if (length == 4) {
      val year = date.substring(0, length)
      inKey = s"${bigscreen}:${year}:in:${organizId}"
      outKey = s"${bigscreen}:${year}:out:${organizId}"
      key = s"${year}_${organizId}"
      condition = year + "12"
    } else if (length == 6) {
      val year = date.substring(0, 4)
      val month = date.substring(4, length)
      inKey = s"${bigscreen}:${year}:${month}:in:${organizId}"
      outKey = s"${bigscreen}:${year}:${month}:out:${organizId}"
      key = s"${year}${month}_${organizId}"
      defaultMap = MONTH_MAP
      if (date == DateUtils.getCurrentDay("yyyyMM")) {
        condition = DateUtils.getCurrentDay("dd")
      } else {
        condition = DateUtils.getMonthLastDay(date)
      }

    } else if (length == 8) {
      val year = date.substring(0, 4)
      val month = date.substring(4, 6)
      val day = date.substring(6, length)
      inKey = s"${bigscreen}:${year}:${month}:${day}:in:${organizId}"
      outKey = s"${bigscreen}:${year}:${month}:${day}:out:${organizId}"
      key = s"${year}${month}${day}_${organizId}"
      defaultMap = DAY_MAP
      if (date < DateUtils.getCurrentDay("yyyyMMdd")) {
        condition = "2400"
      } else {
        condition = DateUtils.getCurrentDay("HHmm")
      }
    } else {
      throw new Exception
    }

    val inRedisMap: util.Map[String, String] = jedis.hgetAll(inKey)
    val inMap: Map[String, Long] = ListMap((defaultMap ++ inRedisMap.map(t => {
      val default: Long = defaultMap.getOrElse(t._1, 0L)
      t._1 -> (t._2.toLong + default)
    })).toSeq.sortBy(_._1): _*).filter(_._1 <= condition)

    val outRedisMap: mutable.Map[String, String] = jedis.hgetAll(outKey)
    val outMap: Map[String, Long] = ListMap((defaultMap ++ outRedisMap.map(t => {
      val default: Long = defaultMap.getOrElse(t._1, 0L)
      t._1 -> (t._2.toLong + default)
    })).toSeq.sortBy(_._1): _*).filter(_._1 <= condition)

    //合并map
    val totalMap: Map[String, Long] = inMap ++ outMap.map(t => {
      val default: Long = inMap.getOrElse(t._1, 0L)
      t._1 -> (t._2.toLong + default.toLong)
    })

    var totalCount: Long = 0L
    totalMap.foreach(m => {
      totalCount += m._2.toLong
    })
    var inTotalCount: Long = 0L
    inMap.foreach(m => {
      inTotalCount += m._2.toLong
    })
    var outTotalCount: Long = 0L
    outMap.foreach(m => {
      outTotalCount += m._2.toLong
    })

    //    (年2019_org1, 该年累计, 该年每月总计,该年每月进总计,该年每月出总计)
    //    (月201907_org1, 该月累计, 该月每天总计,该月每天进总计,该月每天出总计)
    //    (某日20190708_org1, 该天累计, 该天每半小时总计,该天每半小时进总计,该天每半小时出总计)
//        println((key, totalCount, totalMap, inTotalCount, inMap, outTotalCount, outMap))
    (key, totalCount, totalMap, inTotalCount, inMap, outTotalCount, outMap)
  }

  /**
    * 全岛统计
    *
    * @param jedis
    * @param redisNamespace
    * @return
    */
  def getBigScreenStatResult(jedis: Jedis): String = {

    //获取全岛组织ID
    val organizId: String = ConfigUtils.getProperty("redis.bigscreen.idland.id")

    //获取当天日期
    val day: String = DateUtils.getCurrentDay("yyyyMMdd")
    //获取当月
    val month: String = DateUtils.getCurrentDay("yyyyMM")
    //获取上月
    val lastMonth: String = DateUtils.getAppointMonth("yyyyMM", -1)
    //获取当年
    val year: String = DateUtils.getCurrentDay("yyyy")


    //TODO:读取Redis中大屏相关指标统计结果
    val dayTuple: (String, Long, Map[String, Long], Long, Map[String, Long], Long, Map[String, Long]) = getBigScreenStat(jedis, organizId, day)

    val monthTuple: (String, Long, Map[String, Long], Long, Map[String, Long], Long, Map[String, Long]) = getBigScreenStat(jedis, organizId, month)

    val lastMonthTuple: (String, Long, Map[String, Long], Long, Map[String, Long], Long, Map[String, Long]) = getBigScreenStat(jedis, organizId, lastMonth)

    val yearTuple: (String, Long, Map[String, Long], Long, Map[String, Long], Long, Map[String, Long]) = getBigScreenStat(jedis, organizId, year)

    //全年游客累计 yearIntotal
//    println(s"全年游客累计: ${yearTuple._4}") //当月进岛游客累计 monthInTotal
//    println(s"当月进岛游客累计: ${monthTuple._4}") //当日累计进岛人数 dayInTotal
//    println(s"当日累计进岛人数: ${dayTuple._4}") //当日出岛人数 dayOutTotal
//    println(s"当日出岛人数${dayTuple._6}") //实时在岛人数 dayOnTotal
//    println(s"实时在岛人数: ${dayTuple._4 - dayTuple._6}") //当日各时段进岛人数分布图 halfhourDistribute
//    println(s"当日各时段进岛人数分布图: ${dayTuple._5}") //本月进岛人数走势 monthDistribute
//    println(s"本月进岛人数走势: ${monthTuple._5}") //上月进岛人数 lastMonthDistribute
//    println(s"上月进岛人数走势: ${lastMonthTuple._5}")
//    //未来7天进岛人数预测 sevenDayForecast
    val sevenDayForecast: List[Long] = getSevenDayForecast(jedis, organizId)
//    println(s"未来7天进岛人数预测: ${sevenDayForecast}")


    val gson = new Gson()
    println("=====================================================================================================")
    val out = BigScreenStatOut(yearTuple._4, monthTuple._4, dayTuple._4, dayTuple._6, dayTuple._4 - dayTuple._6, dayTuple._5, monthTuple._5, lastMonthTuple._5, sevenDayForecast)
    val message: CommonMessage = constructPassengerFlowToBigScreen(out)
    println(s"输出json=====${gson.toJson(message)}")
    gson.toJson(message)
  }

  /**
    * 构建客流统计数据输出到大屏对象
    * @param payload
    * @return
    */
  def constructPassengerFlowToBigScreen(payload:Object): CommonMessage ={
    val uuid: String = UUID.randomUUID().toString.replace("-","").toUpperCase
    val timestrap: String = DateUtils.getCurrentDay().toString
    val header: Header = new Header (
      uuid, timestrap, HeaderConstant.PASSENGET_FLOW_VERSION,
      HeaderConstant.PASSENGET_FLOW_PRODUCTID,
      HeaderConstant.PASSENGET_FLOW_SOURCEAPPID,
      HeaderConstant.PASSENGET_FLOW_DATAFAMILY
    )
    new CommonMessage(header, payload)
  }

  def getSevenDayForecast(jedis: Jedis, organizId: String): List[Long] = {
    var SevenDayForecast = scala.collection.mutable.ArrayBuffer[Long]()

    for (i <- 1 until 8) {
      var count = 0l
      for (j <- 1 until 8) {
        if (!(i == 7 && j == 1)) {
          //          println(s"==前${j}周===${DateUtils.getAppointDay("yyyyMMdd", -7 * j + i)}")
          val day: String = DateUtils.getAppointDay("yyyyMMdd", -7 * j + i)
          count += getBigScreenStat(jedis, organizId, day)._4
        }
      } //      println(s"==========================未来第${i}天===========$count=====================")
      if (i == 7) {
        SevenDayForecast ++= Array(count / 6)
      } else {
        SevenDayForecast ++= Array(count / 7)
      }
    }
    SevenDayForecast.toList
  }
}


case class BigScreenStatOut(yearIntotal: Long, monthInTotal: Long, dayInTotal: Long, dayOutTotal: Long, dayOnTotal: Long, halfhourDistribute: util.Map[String, Long], monthDistribute: util.Map[String, Long], lastMonthDistribute: util.Map[String, Long], sevenDayForecast: util.List[Long]) extends Serializable;

//全年游客累计 yearIntotal
//当月进岛游客累计 monthInTotal
//当日累计进岛人数 dayInTotal
//当日出岛人数 dayOutTotal
//实时在岛人数 dayOnTotal
//当日各时段进岛人数分布图 halfhourDistribute
//本月进岛人数走势 monthDistribute
//上月进岛人数 lastMonthDistribute
//未来7天进岛人数预测 sevenDayForecast