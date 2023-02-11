package com.mn.livy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.livy.LivyClient;
import org.apache.livy.LivyClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @program:hadoop
 * @description
 * @author:miaoneng
 * @create:2023-02-05 20:18
 **/
public class BatchDemo {
    private final static Logger logger = LoggerFactory.getLogger(BatchDemo.class);
    public static void main(String[] args) throws IOException, URISyntaxException {
        String livyUrl = "http://192.168.52.101:18998";

        Map<String ,Object> params = new HashMap<>();
        params.put("file","/opt/livy/tmpfile/jars/spark-1.0-SNAPSHOT.jar");
        params.put("className","com.mn.demo.WordCount");
        List<String > t = new ArrayList<>();
        t.add("/opt/test.txt");
        t.add("/opt/result");
        params.put("args",t);
        String ret = post(livyUrl + "/batches",new JSONObject(params).toString());
        Map<String, Object> v = (Map<String, Object>) JSON.parse(ret);
        String sessionId = v.get("id").toString();
        logger.info(sessionId);
    }

    public static String post(String url, String jsonStr) throws IOException {
        String mssg;
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(30000).setConnectionRequestTimeout(30000)
                .setSocketTimeout(60000).build();
        httpPost.setConfig(requestConfig);
        httpPost.setHeader("Content-type", "application/json");
        CloseableHttpResponse httpResponse = null;

        try {
            httpPost.setEntity(new StringEntity(jsonStr, "utf-8"));
            httpResponse = httpClient.execute(httpPost);
            HttpEntity httpEntity = httpResponse.getEntity();
            return EntityUtils.toString(httpEntity);
        } catch (IOException e) {
            throw e;
        } finally {
            try {
                if (httpResponse != null) {
                    httpResponse.close();
                }
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
