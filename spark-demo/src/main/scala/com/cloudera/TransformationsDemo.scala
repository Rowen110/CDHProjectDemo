package com.cloudera

import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.sql.SparkSession


/**
  * Create by Charles
  * Date 2019/4/1 17:36
  * Description
  */
object TransformationsDemo {

  def main(args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("Simple Application").setMaster("local").set("spark.submit.deployMode", "client")
    //      .set("spark.master", "yarn")
    //      .set("spark.submit.deployMode", "client")
    //
    //    //初始化SparkSession对象
    //    val spark = SparkSession.builder().config(conf).getOrCreate()
    ////    val spark : SparkSession = SparkSession.builder.appName("Simple Application").getOrCreate()
    //
    //    val xs: List[Int] = (1 to 10000).toList
    //
    //    val rdd: RDD[Int] = spark.sparkContext.parallelize(xs)
    //
    //
    //    rdd.collect.foreach(println)

    //    val conf = new SparkConf()
    //    conf.setAppName("debug mode")
    //    //conf.setMaster("local[2]")
    //    conf.setMaster("yarn-cluster")

    val builder = SparkSession.builder().config(conf)
    val sparkSession = builder.getOrCreate()
    val sc = sparkSession.sparkContext
    val list = List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    val rdd = sc.parallelize(list)
    rdd.foreach(f => println(f))
    while (true) {
      rdd.foreach(f => println(f))
      Thread.sleep(3000)
    }
  }
}
