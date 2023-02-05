# ELasticsearch 聚合排序不准分析处理

​		ELasticsearch进行聚合排序后取TopN，对于每个分片都会取TopN，然后分片间再进行TopN，这种会导致结果不准。

​		接下去我们按照步骤来测试下，同时也看下整个过程。

# 1、安装ELasticsearch

ELasticsearch

```
https://www.elastic.co/cn/downloads/past-releases/elasticsearch-7-1-1
```

Kibana

```
https://www.elastic.co/cn/downloads/past-releases/kibana-7-1-1
```

# 2、jdk安装

略

# 3、修改hostname

```
vi /etc/hostname
node1
reboot
```

# 4、修改系统配置文件

```
vi /etc/sysctl.conf
```

```
vm.swappiness=1
vm.max_map_count=262144
```

```
vi /etc/security/limits.conf
```

```
# 进程线程数
* soft nproc 131072
* hard nproc 131072
# 文件句柄数
* soft nofile 131072
* hard nofile 131072
# 内存锁定交换
* soft memlock unlimited
* hard memlock unlimited
```

# 5、创建ES专用账号

```
#创建 ES 账号，如 elastic
groupadd esgroup
useradd elastic -g esgroup
passwd elastic
```

# 6、解压

```
tar -xzvf elasticsearch-7.1.1-linux-x86_64.tar.gz -C /opt/
```

# 7、新建数据目录与日志目录

```
mkdir -p /opt/elasticsearch/data
mkdir -p /opt/elasticsearch/log
```

# 8、授权

```
chown -R elastic:esgroup /opt/elasticsearch-7.1.1
chown -R elastic:esgroup /opt/elasticsearch-7.1.1/*
chown -R elastic:esgroup /opt/elasticsearch/*
```

# 9、修改es环境配置

```
cd /opt/elasticsearch-7.1.1/config

vi elasticsearch.yml
```

```
 cluster.name: es
 node.name: node1
 network.host: 192.168.52.200
 http.port: 9200
 transport.port: 9300
 path.data: /opt/elasticsearch/data
 path.logs: /opt/elasticsearch/log
 discovery.seed_hosts: ["node1:9300"]
 cluster.initial_master_nodes: ["node1:9300"]
```

# 10、必要的话还可以修改下内存，改为机器内存的一半

```
vi jvm.options
```

# 11、启动es服务

```
# 需切换为es用户
su elastic
#进入bin目录
cd /opt/elasticsearch-7.1.1/bin
# 设置后台启动
sh elasticsearch -d
```

# 12、启动

![img](https://docimg1.docs.qq.com/image/AgAABS4iltCGadRTQOdMdLXzyXnOTghJ.png?w=508&h=418)

# 13、安装配置kibana

kibana下载地址

```
https://www.elastic.co/cn/downloads/past-releases/kibana-7-1-1
```

注意kibana版本需要和ELasticsearch版本一致

```
tar -xvf kibana-7.1.1-linux-x86_64.tar.gz -C /opt/

cd /opt/kibana-7.1.1-linux-x86_64/config

vi kibana.yml
```

```
server.port: 5601
server.host: "192.168.52.200"
elasticsearch.hosts: ["http://192.168.52.200:9200"]
kibana.index: ".kibana"
```

# 14、启动kibana

```
//切换到root用户下
su root
//修改kibana文件权限
chown -R elastic:esgroup /opt/kibana-7.1.1-linux-x86_64
chown -R elastic:esgroup /opt/kibana-7.1.1-linux-x86_64/*
//切换用户
su elastic
//进入kibana的bin下
cd /opt/kibana-7.1.1-linux-x86_64/bin
nohup ./kibana &
//访问
http://192.168.52.200:5601/
```

提示：’如果想关闭kibana，可以直接看下

```
netstat -anp|grep 5601
kill -9 进程号
```

![img](https://docimg10.docs.qq.com/image/AgAABS4iltAT-UQXm0JL2KJYqdkwVgA_.png?w=1170&h=871)



# 15、新建索引

我们模拟一个学生选课程的场景。用Elasticsearch提取选课前5的课程。

场景：

1、假设有全校一共有5000个人。
2、其中3000人，选了前100门课，每门被选了30次。

3、其实2000人选了100后的2000门课程，每门课程被选1次。

新建索引，3分片，1副本

```
HttpHost host = new HttpHost("192.168.52.200", 9200);
HttpHost[] hostArray = {host};
EsManger esManger = new EsManger(hostArray);
HashMap<String,String> hashMap = new HashMap<>();
hashMap.put("rowguid","keyword");
hashMap.put("lesson","keyword");

//添加索引
esManger.addIndexAndType("studentinfo",3,1,hashMap);
```

# 16、准备数据

生产数据，为了更快的演示现象，我们使用随机路由。

```
//假设有全校一共有5000个人
//其中3000人，选了前100门课，每门被选了30次
for(int j = 0;j<30;j++){
    for (int i = 0; i < 100; i++) {
        List<Map<String, Object>> result = new ArrayList<>();
        String rowguid = UUID.randomUUID().toString();
        Map<String, Object> map = new HashMap<>();
        map.put("rowguid", rowguid);
        map.put("lesson", i);
        map.put("id_keyword", rowguid);
        result.add(map);
        esManager.addDocument("studentinfo", result, Integer.toString(r.nextInt(10000)));
    }
}

//其实2000人选了100后的2000门课程，每门课程被选1次
for (int i = 100; i < 2100; i++) {
            List<Map<String, Object>> result = new ArrayList<>();
            String rowguid = UUID.randomUUID().toString();
            Map<String, Object> map = new HashMap<>();
            map.put("rowguid", rowguid);
            map.put("lesson", i);
            map.put("id_keyword", rowguid);
            result.add(map);
            esManager.addDocument("studentinfo",  result, Integer.toString(r.nextInt(10000)));
        }

```

可以看到我们存的元素分散在各个分片上。

![img](https://docimg10.docs.qq.com/image/AgAABS4iltBJ25iMCb9EcoZojGYwBEwT.png?w=750&h=646)

# 17、结果

按理说进行聚合得到选课最高的5门课。肯定是前100的。并且统计的count均为30.

进行测试。

```
POST studentinfo/_search
{
  "size":0,
  "aggs": {
    "lesson_aggs": {
      "terms": {
        "field":"lesson",
        "size":5
      }
    }
  }
}
```

结果如下，出乎意料，不对，前五都没有统计到30。

```
{
  "took" : 5,
  "timed_out" : false,
  "_shards" : {
    "total" : 30,
    "successful" : 30,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 5000,
      "relation" : "eq"
    },
    "max_score" : null,
    "hits" : [ ]
  },
  "aggregations" : {
    "lesson_aggs" : {
      "doc_count_error_upper_bound" : 60,
      "sum_other_doc_count" : 4882,
      "buckets" : [
        {
          "key" : "25",
          "doc_count" : 25
        },
        {
          "key" : "11",
          "doc_count" : 24
        },
        {
          "key" : "14",
          "doc_count" : 23
        },
        {
          "key" : "27",
          "doc_count" : 23
        },
        {
          "key" : "28",
          "doc_count" : 23
        }
      ]
    }
  }
}
```

# 18、分析

原因是当elasticsearch进行聚合时会让各个每片返回top5的统计，各个节点上返回的值再进行一次聚合。而得到的值不一定是准确的，可以说是错误的。

# 19、解决

由于elasticsearch计算路由的公式是

```
shard = hash(routing) % number_of_primary_shards
```

所以我们只要保证相同要term的字段的使用同一个路由即可。

```
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
```

# 20、结果

```
{
  "took" : 23,
  "timed_out" : false,
  "_shards" : {
    "total" : 30,
    "successful" : 30,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : {
      "value" : 5000,
      "relation" : "eq"
    },
    "max_score" : null,
    "hits" : [ ]
  },
  "aggregations" : {
    "lesson_aggs" : {
      "doc_count_error_upper_bound" : 30,
      "sum_other_doc_count" : 4850,
      "buckets" : [
        {
          "key" : "0",
          "doc_count" : 30
        },
        {
          "key" : "1",
          "doc_count" : 30
        },
        {
          "key" : "10",
          "doc_count" : 30
        },
        {
          "key" : "11",
          "doc_count" : 30
        },
        {
          "key" : "12",
          "doc_count" : 30
        }
      ]
    }
  }
}
```

