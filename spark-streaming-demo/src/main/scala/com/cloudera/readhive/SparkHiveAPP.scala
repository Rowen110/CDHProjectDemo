package com.cloudera.readhive

import org.apache.log4j.{Level, Logger}
import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession

object SparkHiveAPP {

  def main(args: Array[String]): Unit = {

    Logger.getLogger("org").setLevel(Level.WARN)
    /**
      * 不设置 System.setProperty("HADOOP_USER_NAME", "root") 会出现异常
      * org.apache.hadoop.security.AccessControlException: Permission denied
      */
    System.setProperty("HADOOP_USER_NAME", "root")
    val conf = new SparkConf()
      .setIfMissing("spark.master", "local[2]")
      .set("spark.sql.warehouse.dir", "/user/hive/warehouse")
      .setAppName("Spark_Hive_APP")

    val spark: SparkSession = SparkSession.builder().config(conf)
      .enableHiveSupport()
      .getOrCreate()

    spark.sparkContext.setLogLevel("WARN")

    spark.sql("SELECT * FROM test.test1").show()

  }
}
