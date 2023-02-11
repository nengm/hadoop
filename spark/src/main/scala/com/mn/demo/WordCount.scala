package com.mn.demo

import org.apache.spark.rdd.RDD
import org.apache.spark.{SparkConf, SparkContext}

/**
 * @program:hadoop
 * @description
 * @author:miaoneng
 * @create:2023-02-05 17:28
 **/
object WordCount {
  def main(args: Array[String]): Unit = {
    if(args.length < 2){
      println("要输入input和output")
      System.exit(1);
    }
    //spark上下文执行环境
    val conf:SparkConf = new SparkConf().setAppName("wordcount");
    val sc:SparkContext = new SparkContext(conf);

    val lines:RDD[String]= sc.textFile(args(0))
    val words:RDD[String] = lines.flatMap(_.split(" "))
    val wordAndOnes:RDD[(String,Int)] = words.map((_,1))
    val result:RDD[(String,Int)] = wordAndOnes.reduceByKey(_+_)
    result.repartition(1).saveAsTextFile(args(1))
    sc.stop()
  }
}
