package com.mn.es;

import com.alibaba.fastjson.JSON;
import com.mn.utils.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.nio.entity.NStringEntity;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;
import java.util.*;

/**
 * @program:hadoop
 * @description
 * @author:miaoneng
 * @create:2023-02-04 09:56
 **/
public class EsManager {
    private RestClient restClient = null;

    public EsManager(HttpHost[] hostArray) {
        restClient = getTransportClient(hostArray);
    }

    /*
     * @Description:构建restclient
     * @Param: HostArray
     * @return: org.elasticsearch.client.RestClient
     * @Author: miaoneng
     * @Date: 2023/2/4 10:42
     */
    public RestClient getTransportClient(HttpHost[] hostArray) {
        RestClientBuilder builder = RestClient.builder(hostArray);
        Header[] defaultHeaders = new Header[]{new BasicHeader("Accept", "application/json"),
                new BasicHeader("Content-type", "application/json")};

        builder.setRequestConfigCallback(new RestClientBuilder.RequestConfigCallback() {
            public RequestConfig.Builder customizeRequestConfig(RequestConfig.Builder requestConfigBuilder) {
                return requestConfigBuilder.setConnectTimeout(5000)
                        .setSocketTimeout(60000)
                        .setConnectionRequestTimeout(-1);
            }
        });
        builder.setDefaultHeaders(defaultHeaders);
        return builder.build();
    }

    public boolean addIndexAndType(String index, Integer shardNum, Integer replicaNum, HashMap<String, String> hashMap) {
        Response rsp = null;
        // 创建索引映射,相当于创建数据库中的表操作
        if (isIndexExist(index)) {
            deleteIndex(index);
        }


        //设置索引的mapping
        Map<String, Object> mappings = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();

        Iterator<Map.Entry<String, String>> iterator = hashMap.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<String, String> entry = iterator.next();
            Map<String, Object> map = new HashMap<>();
            String indexColumn = entry.getKey();
            String typeColumn = entry.getValue();
            if (StringUtils.hasText(typeColumn) && StringUtils.hasText(indexColumn)) {
                if (typeColumn.equals("text")) {
                    map.put("type", typeColumn);
                    map.put("analyzer", "ik_max_word");
                } else if (typeColumn.equals("date")) {
                    map.put("type", typeColumn);
                    map.put("format", "yyyy-MM-dd HH:mm:ss");
                } else {
                    map.put("type", typeColumn);
                }
                properties.put(indexColumn, map);
            }
        }
        mappings.put("properties", properties);


        String jsonString = "{\"settings\":{\"number_of_shards\":" + shardNum + ",\"number_of_replicas\":" + replicaNum + "},\"mappings\":" + JSON.toJSONString(mappings) + "}";

        HttpEntity entity = new NStringEntity(jsonString, ContentType.APPLICATION_JSON);
        try {
            Request request = new Request(
                    "PUT",
                    "/" + index);
            request.addParameter("pretty", "true");
            request.setEntity(entity);
            rsp = restClient.performRequest(request);
            if (HttpStatus.SC_OK == rsp.getStatusLine().getStatusCode()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    /*
     * @Description: 判断索引是否存在
     * @Param: index
     * @return: boolean
     * @Author: miaoneng
     * @Date: 2023/2/4 11:22
     */
    public boolean isIndexExist(String index) {
        Response rsp = null;
        try {
            Request request = new Request(
                    "HEAD",
                    "/" + index);
            request.addParameter("pretty", "true");
            rsp = restClient.performRequest(request);
            if (HttpStatus.SC_OK == rsp.getStatusLine().getStatusCode()) {
                return true;
            }
            if (HttpStatus.SC_NOT_FOUND == rsp.getStatusLine().getStatusCode()) {
                return false;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * @Description:删除索引
     * @Param: index
     * @return: void
     * @Author: miaoneng
     * @Date: 2023/2/4 11:22
     */
    public void deleteIndex(String index) {
        try {
            Request request = new Request(
                    "DELETE",
                    "/" + index);
            request.addParameter("pretty", "true");
            restClient.performRequest(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void addDocument(String index, List<Map<String, Object>> result, String routeFlag) {
        Response rsp = null;
        StringBuffer buffer = new StringBuffer();
        String str = "";
        str = "{ \"index\" : { \"_index\" : \"" + index + "\"";
        for (Map<String, Object> mmap : result) {
            Map<String, Object> map = new HashMap<>();
            for (Map.Entry<String, Object> entry : mmap.entrySet()) {
                Object value = entry.getValue();
                String key = entry.getKey();
                map.put(key, value);
            }
            buffer.append(str).append(",\"_id\":\"" + mmap.get("id_keyword") + "\" } }").append("\n");
            buffer.append(JSON.toJSONString(map)).append("\n");
        }
        StringEntity entity = null;
        entity = new StringEntity(buffer.toString(), ContentType.APPLICATION_JSON);
        entity.setContentEncoding("UTF-8");
        Map<String, String> params = Collections.singletonMap("pretty", "true");
        try {
            Request request = new Request(
                    "PUT",
                    "/_bulk?routing=" + routeFlag);
            request.addParameter("pretty", "true");
            request.setEntity(entity);
            Response response = restClient.performRequest(request);
        } catch (Exception e) {
            //防止有的时候拿不到连接>_<
            boolean flag = true;
            for (int i = 0; i < 5; i++) {
                Request request = new Request(
                        "PUT",
                        "/_bulk");
                request.addParameter("pretty", "true");
                request.setEntity(entity);
                Response response = null;
                try {
                    response = restClient.performRequest(request);
                    flag = true;
                } catch (IOException ex) {
                    flag = false;
                    if (i == 4) {
                        ex.printStackTrace();
                    }
                }
                if (flag) {
                    i = 4;
                }
            }

        }
    }
    public void destroy() {
        try {
            if (restClient != null) {
                restClient.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
