# hadoop
存放工作学习中遇到的大数据或者大数据相关中间件的解决方案，用于学习复习分享，简简单单，平平淡淡。



# 1、ELasticsearch 聚合排序不准分析处理

​		ELasticsearch进行聚合排序后取TopN，对于每个分片都会取TopN，然后分片间再进行TopN，这种会导致结果不准。

[ELasticsearch 聚合排序不准分析处理](https://github.com/nengm/hadoop/blob/main/elasticsearch/README.md)

# 2、livy分析应用实战

​		Apache Livy提供Rest service来与Apache Spark进行交互，通过Rest interface或RPC client来简化spark job和spark code snippet的提交，同步或异步获取结果，并提供对spark context的管理。

[全方位代码层面讲解livy使用](https://github.com/nengm/hadoop/blob/main/livy/README.md)



