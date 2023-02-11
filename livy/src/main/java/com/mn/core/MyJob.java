package com.mn.core;

import com.mn.demo.DataOperate;
import org.apache.livy.Job;
import org.apache.livy.JobContext;
import org.apache.spark.sql.SparkSession;

/**
 * @program:hadoop
 * @description 作业
 * @author:miaoneng
 * @create:2023-02-11 13:19
 **/
public class MyJob implements Job<Integer> {
    // 方法名
    private String methodName = "";

    // 所存步骤参数
    private String params;

    public MyJob(String methodName, String params) {
        this.methodName = methodName;
        this.params = params;
    }

    @Override
    public Integer call(JobContext jobContext) throws Exception {
        //让spark算子包进行计算
        return callSpark(jobContext.sparkSession(), methodName, params);
    }

    public Integer callSpark(SparkSession sparkSession, String methodName, String params) {
        return DataOperate.operateData(sparkSession, methodName, params);
    }
}
