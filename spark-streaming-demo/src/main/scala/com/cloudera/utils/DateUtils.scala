package com.cloudera.utils

import java.text.SimpleDateFormat
import java.util.{Calendar, Date}

import org.apache.kafka.common.TopicPartition

object DateUtils {
  /**
    * 获取今天日期
    * @param format
    * @return
    */
  def getCurrentDay(format:String): String ={
    new SimpleDateFormat(format).format(new Date())
  }

  /**
    * 获取当前时间戳日期
    * @param format
    * @return
    */
  def getCurrentDay(): Long ={
    new Date().getTime
  }

  /**
    * 获取指定日期
    * @param format 格式字符串 例如： yyyy-MM-dd
    * @param num  -1 表示昨日， 1表示明日
    * @return
    */
  def getAppointDay(format:String,num: Int): String ={
    var cal: Calendar = Calendar.getInstance()
    cal.add(Calendar.DATE,num)
    new SimpleDateFormat(format).format(cal.getTime())
  }


  def getMonthLastDay(date: String): String ={

    val year = date.substring(0,4).toInt;
    val month = date.substring(4,6).toInt;
    val cal = Calendar.getInstance();
    cal.set(year,month,0);
    cal.get(Calendar.DAY_OF_MONTH).toString
  }

  /**
    * 获取指定月
    * @param format 格式字符串 例如： yyyy-MM
    * @param num  -1 表示上月， 1表示下月
    * @return
    */
  def getAppointMonth(format:String,num: Int): String ={
    var cal: Calendar = Calendar.getInstance()
    cal.add(Calendar.MONTH,num)
    new SimpleDateFormat(format).format(cal.getTime())
  }


  def main(args: Array[String]): Unit = {
      val topics = Map("test1"-> 2,"test2"-> 3,"test3"-> 4)
      var fromOffsets: Map[TopicPartition, Long] = Map()


    topics.foreach(topic => {
      println(topic)
      for (i <- 0 until topic._2.toInt) {
        val topic_partition_key = topic + "_" + i

      }
    })
//      for (topic <- topics){
//        val lastSavedOffset = 0l
//        for (i <- 0 until partition) {
//          val topic_partition_key = topic + "_" + i
//          try {
//            fromOffsets += ((new TopicPartition(topic, i) -> lastSavedOffset.toLong))
//          } catch {
//            case ex: Exception => println(ex.getMessage)
//              println("get lastSavedOffset error, lastSavedOffset from redis [" + lastSavedOffset + "] ")
//              System.exit(1)
//          }
//        }
//      }
    println(fromOffsets)
  }
}
