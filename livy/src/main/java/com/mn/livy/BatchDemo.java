package com.mn.livy;

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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @program:hadoop
 * @description
 * @author:miaoneng
 * @create:2023-02-05 20:18
 **/
public class BatchDemo {
    public static void main(String[] args) throws IOException, URISyntaxException {
        String livyUrl = "192.168.52.101";
        //未完待续


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
