package io.debezium.connector.oracle.logminer.processor.rocksdb;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.rocksdb.Options;
import org.rocksdb.RocksDB;

import java.nio.charset.StandardCharsets;

public class RocksDbHdfs {
  // 文件在HDFS上的路径
  private static final String HDFS_PATH = "/tmp/testdb";
  // RocksDB数据库的名称
  private static final String DB_NAME = "mydb";

  public static void main(String[] args) {
    // 创建HDFS文件系统对象
    Configuration config = new Configuration();
    try (FileSystem hdfs = FileSystem.get(config)) {
      // 创建RocksDB选项对象
      Options options = new Options();
      options.setCreateIfMissing(true);

      // 打开RocksDB数据库
      RocksDB db = RocksDB.open(options, HDFS_PATH + "/" + DB_NAME);

      // 写入数据
      db.put("key".getBytes(StandardCharsets.UTF_8), "value".getBytes(StandardCharsets.UTF_8));

      // 读取数据
      byte[] value = db.get("key".getBytes(StandardCharsets.UTF_8));
      System.out.println(new String(value, StandardCharsets.UTF_8));

      // 关闭数据库
      db.close();
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
    }
  }
}