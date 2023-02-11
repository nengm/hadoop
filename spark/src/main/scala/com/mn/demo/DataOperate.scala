package com.mn.demo

import com.alibaba.fastjson.{JSON, JSONObject}
import org.apache.spark.sql.SparkSession

/**
 * @program:hadoop
 * @description
 * @author:miaoneng
 * @create:2023-02-11 12:29
 **/
object DataOperate {
  def operateData(spark:SparkSession,stepId:String,jsonDataStr:String):Int ={
    println(stepId)
    println(jsonDataStr)
    var obj:JSONObject = JSON.parse(jsonDataStr).asInstanceOf[JSONObject]
    val x = obj.get("x").asInstanceOf[Int]
    val y = obj.get("y").asInstanceOf[Int]
    if(stepId=="add"){
      add(x,y)

    }else if(stepId=="sub"){
      sub(x,y)
    }else{
      0
    }
  }
  def add(x:Int,y:Int): Int ={
    x+y
  }

  def sub(x:Int,y:Int):Int= {
    x-y
  }
}
