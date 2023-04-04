package io.debezium.connector.oracle.logminer;

import org.rocksdb.Options;
import org.rocksdb.RocksDB;
import org.rocksdb.RocksDBException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nepkisa
 * @date 2023/04/04 23:32:51
 */
public class RocksDbCache {
    // the Options class contains a set of configurable DB options
    // that determines the behaviour of the database.
    private static Options options = new Options().setCreateIfMissing(true);
    private static RocksDB db;
    private static String path;
    private static Map<String, Integer> counter = new HashMap<>();

    static {
        // a static method that loads the RocksDB C++ library.
        RocksDB.loadLibrary();
        try {
            if (path == null || "".equals(path.trim())) {
                path = "rocksdb-data";
            }
            db = RocksDB.open(RocksDbCache.options, path);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    //TODO where close rocksdb connect?
    public static void close() {
        db.close();
    }

    public static void setPath(String uri) {
        path = uri;
    }

    public static Map<String, Integer> getCounter() {
        return counter;
    }

    public static Object get(Object key) {
        try {
            byte[] kb = objectToByte(key);
            return byteToObject(db.get(kb));
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public static void put(Object key, Object value) {
        try {
            byte[] kb = objectToByte(key);
            byte[] vb = objectToByte(value);
            db.put(kb, vb);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    public static void delete(Object key) {
        try {
            byte[] kb = objectToByte(key);
            db.delete(kb);
        } catch (RocksDBException e) {
            throw new RuntimeException(e);
        }
    }

    //Deserialized classes need implements Serializable recursively
    private static Object byteToObject(byte[] bytes) {
        Object obj = null;
        try {
            //bytearray to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);
            obj = oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception ae) {
            ae.printStackTrace();
        }
        return obj;
    }


    public static byte[] objectToByte(Object obj) {
        if (obj == null) return null;
        byte[] bytes = new byte[0];
        try {
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);
            bytes = bo.toByteArray();
            bo.close();
            oo.close();
        } catch (Exception ae) {
            ae.printStackTrace();
        }
        return (bytes);
    }
}
