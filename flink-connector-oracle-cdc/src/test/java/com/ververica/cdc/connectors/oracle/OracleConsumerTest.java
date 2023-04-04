package com.ververica.cdc.connectors.oracle;


import com.ververica.cdc.connectors.base.options.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.junit.Test;

import java.util.Properties;

/**
 * @author Nepkisa
 * @date 2023/04/04 23:05:38
 */
public class OracleConsumerTest {

    @Test
   public void testA(){
        //1.创建执行环境
        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
/*
        //2.Flink-CDC 将读取 binlog 的位置信息以状态的方式保存在 CK,如果想要做到断点续传,需要从 Checkpoint 或者 Savepoint 启动程序
        //2.1 开启 Checkpoint,每隔 5 秒钟做一次 CK
        env.enableCheckpointing(5000L);
        //2.2 指定 CK 的一致性语义
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        //2.3 设置任务关闭的时候保留最后一次 CK 数据
        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);
        //2.4 指定从 CK 自动重启策略
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, 2000L));
        //2.5 设置状态后端
        env.setStateBackend(new FsStateBackend("hdfs://192.168.10.130:9000/flinkCDC"));
        //2.6 设置访问 HDFS 的用户名
        System.setProperty("HADOOP_USER_NAME", "nepkisa");*/

        Properties kafkaProperties = new Properties();
        kafkaProperties.put("bootstrap.servers", "192.168.10.130:9092");
        Properties debeziumProperties = new Properties();
        debeziumProperties.put("debezium.log.mining.strategy", "online_catalog");
        debeziumProperties.put("decimal.handling.mode", "double");
        SourceFunction<String> sourceFunction = OracleSource.<String>builder()
//                .startupOptions(StartupOptions.latest())
                .hostname("192.168.10.130")
                .port(1521)
                .database("nep") // monitor nep database
                .schemaList("neptune") // monitor neptune schema
                .tableList("neptune.userx") // monitor student table
                .username("cdcuser")
                .password("123456")
                .debeziumProperties(debeziumProperties)//可兼容debezium的参数
                .deserializer(new JsonDebeziumDeserializationSchema()) // converts SourceRecord to JSON String
                .build();

        //输出到kafka
//        env.addSource(sourceFunction).addSink(
//                new FlinkKafkaProducer<String>("oracle-debezium", new SimpleStringSchema(), kafkaProperties)
//        );

        env.addSource(sourceFunction).print(); // use parallelism 1 for sink to keep message ordering

        try {
            env.execute();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
   }
}
