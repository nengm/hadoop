# livy分析应用实战

​		Apache Livy提供Rest service来与Apache Spark进行交互，通过Rest interface或RPC client来简化spark job和spark code snippet的提交，同步或异步获取结果，并提供对spark context的管理。

​		看下llivy官网的图，我们可以大概了解到livy的工作过程，其实就是充当了当我们提交作业到spark上的这个角色。以前我们直接要到有spark client的机器上进行submit的操作，现在我们只要使用restful就能完成所有的操作。

![img](https://docimg10.docs.qq.com/image/AgAABS4iltCfxr4hHu5MY6fQxAO7_evd.png?w=800&h=425)

​		livy有诸多特点，接下去我们一一去分析应用。

# 1、livy安装

1、下载

```
https://dlcdn.apache.org/incubator/livy/0.7.1-incubating/apache-livy-0.7.1-incubating-bin.zip
```

2、安装

```
unzip apache-livy-0.7.1-incubating-bin.zip
cd /opt
mv apache-livy-0.7.1-incubating-bin livy
```

3、配置

```
cd /opt/livy
mkdir logs
mkdir tmfile
cd /opt/livy/conf
cp livy.conf.template livy.conf
cp livy-client.conf.template livy-client.conf
cp livy-env.sh.template livy-env.sh
cp log4j.properties.template log4j.properties
```

vi livy.conf

```
livy.server.port = 18998
livy.server.host = 192.168.52.101
livy.spark.master = spark://192.168.52.101:7077
livy.server.session.timeout = 1h
livy.file.local-dir-whitelist = /opt/livy/tmpfile
livy.session.staging-dir=/opt/livy/tmpfile/tmp
livy.spark.deploy-mode = client
livy.repl.enable-hive-context = true
```

vi livy-env.sh

```
#java路径
export JAVA_HOME=/usr/local/jdk
#spark路径
export SPARK_HOME=/opt/spark
#livyserver内存
export LIVY SERVER JAVA OPTS="-XmX1G"
#livy log存放位置
export LIVY_LOG_DIR=/opt/livy/logs
#livy PID文件存放位置
export LIVY_PID_DIR=/opt/livy/tmpfile/tmp
export LIVY_TEST=true

```

4、启动

```
cd /opt/livy/bin
./livy-server start
```

![image-20230205144319324](C:\Users\mn\AppData\Roaming\Typora\typora-user-images\image-20230205144319324.png)![img](https://docimg3.docs.qq.com/image/AgAABS4iltCUQbek3rJE25VE44cShSe9.png?w=1537&h=568)



# 1、livy会话类型

livy包括两种会话模型，我们一一通过代码去分析。

1、batch 方式

​	用户可以通过Livy以批处理的方式启动Spark应用，与spark中的sumbit是一样的，livy中就叫batch session

未完待续