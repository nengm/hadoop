package com.mn.demo;

import com.mn.es.EsManager;
import org.apache.http.HttpHost;

import java.util.*;

/**
 * @program:hadoop
 * @description
 * @author:miaoneng
 * @create:2023-02-04 11:24
 **/
public class TermTest {

    public static void main(String[] args) {
        HttpHost host = new HttpHost("192.168.52.200", 9200);
        HttpHost[] hostArray = {host};
        EsManager esManager = new EsManager(hostArray);
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("rowguid", "keyword");
        hashMap.put("lesson", "keyword");

        //添加索引
        esManager.addIndexAndType("studentinfo", 30, 1, hashMap);
        Random r = new Random();
//        for(int j = 0;j<30;j++){
//            for (int i = 0; i < 100; i++) {
//                List<Map<String, Object>> result = new ArrayList<>();
//                String rowguid = UUID.randomUUID().toString();
//                Map<String, Object> map = new HashMap<>();
//                map.put("rowguid", rowguid);
//                map.put("lesson", i);
//                map.put("id_keyword", rowguid);
//                result.add(map);
//                esManager.addDocument("studentinfo", result, Integer.toString(r.nextInt(10000)));
//            }
//        }
//
//        for (int i = 100; i < 2100; i++) {
//            List<Map<String, Object>> result = new ArrayList<>();
//            String rowguid = UUID.randomUUID().toString();
//            Map<String, Object> map = new HashMap<>();
//            map.put("rowguid", rowguid);
//            map.put("lesson", i);
//            map.put("id_keyword", rowguid);
//            result.add(map);
//            esManager.addDocument("studentinfo",  result, Integer.toString(r.nextInt(10000)));
//        }

        for(int j = 0;j<30;j++){
            for (int i = 0; i < 100; i++) {
                List<Map<String, Object>> result = new ArrayList<>();
                String rowguid = UUID.randomUUID().toString();
                Map<String, Object> map = new HashMap<>();
                map.put("rowguid", rowguid);
                map.put("lesson", i);
                map.put("id_keyword", rowguid);
                result.add(map);
                esManager.addDocument("studentinfo", result, Integer.toString(i));
            }
        }

        for (int i = 100; i < 2100; i++) {
            List<Map<String, Object>> result = new ArrayList<>();
            String rowguid = UUID.randomUUID().toString();
            Map<String, Object> map = new HashMap<>();
            map.put("rowguid", rowguid);
            map.put("lesson", i);
            map.put("id_keyword", rowguid);
            result.add(map);
            esManager.addDocument("studentinfo",  result, Integer.toString(i));
        }
        esManager.destroy();
    }
}
