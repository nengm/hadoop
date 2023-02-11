package com.mn.livy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.mn.core.MyJob;
import org.apache.livy.LivyClient;
import org.apache.livy.LivyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * @program:hadoop
 * @description
 * @author:miaoneng
 * @create:2023-02-11 13:31
 **/
public class SeesionDemo {
    private final static Logger logger = LoggerFactory.getLogger(SeesionDemo.class);
    public static String url = "http://192.168.52.101:18998";
    public static void main(String[] args) {

        //定义步骤1，调用add方法，两个参数为x:10，y:10
        String jsonString1 =  "{\"methodName\":\"add\",\"params\":{\"x\":10,\"y\":10}}";

        //定义步骤2，调用sub方法，两个参数为x:20，y:10
        String jsonString2 =  "{\"methodName\":\"sub\",\"params\":{\"x\":20,\"y\":10}}";

        List<String> stepList = new ArrayList<>();
        stepList.add(jsonString1);
        stepList.add(jsonString2);
        try {
            callSpark(stepList);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public static void callSpark(List<String> stepList) throws IOException, URISyntaxException, ExecutionException, InterruptedException {

        LivyClient livyClient = new LivyClientBuilder()
                .setURI(new URI(url))
                .build();
        //换成你自己的路径
        //用livy客户端将我们写的Spark的算子包提交到spark环境中
        livyClient.uploadJar(new File("E:\\github\\hadoop\\spark\\target\\spark-1.0-SNAPSHOT.jar")).get();

        //livy写的业务操作jar
        livyClient.uploadJar(new File("E:\\github\\hadoop\\livy\\target\\livy-1.0-SNAPSHOT.jar")).get();

        //spark.jar中用到的fastjson包
        //1、可以在打spark.jar的时候可以打成一个胖jar包
        //2、可以写个代码把要上传的jar规范好，让大家按照这个格式列好spark.jar用到的额外jar，然后一个个upload上去
        livyClient.uploadJar(new File("E:\\github\\jars\\fastjson-1.2.83.jar")).get();


        //拆解需要执行的步骤
        Integer ret = 0;
        for(int i =0 ;i < stepList.size();i++){
            JSONObject jsonObject = JSON.parseObject(stepList.get(i));
            String methodName = jsonObject.get("methodName").toString();
            String params = jsonObject.get("params").toString();
            ret = livyClient.submit(new MyJob(methodName,params)).get();
            logger.info("步骤"+i+":所做的操作是"+methodName+",结果为:"+ret);
        }
        livyClient.stop(true);
    }
}
