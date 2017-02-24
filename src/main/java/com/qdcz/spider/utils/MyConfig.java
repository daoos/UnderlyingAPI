package com.qdcz.spider.utils;

/**
 * Global configuration
 * @author jd
 *
 */
public class MyConfig {
	/**
	 * MYSQL connector configuration
	 */
	public static String MYSQL_HOST = "192.168.116.11";
	public static int MYSQL_PORT = 3306;

	public static String MYSQL_USER = "root";
	public static String MYSQL_PASSWORD = "&hpre-sc&";
	public static String MYSQL_CHARSET = "utf-8";
	public static String MYSQL_DBNAME = "yuqing_db";
	
	/**
	 * REDIS connector configuration
	 */
	public static String REDIS_HOST = "192.168.113.93";
	public static int REDIS_PORT = 6379;
	public static int REDIS_DB_INDEX = 0;
	
	/**
	 * MONGODB connector configuration
	 * route by storm nimbus
	 */
	public static String MONGODB_HOST = "192.168.113.80";
	public static int MONGODB_PORT = 27017;
	
	/**
	 * KAFKA cluster configuration
	 */
	public static String KAFKA_METADATA_BROKER_LIST = "192.168.113.60:9092,192.168.113.61:9092,192.168.113.62:9092,192.168.113.63:9092";
	
	/**
	 * ZOOKEEPER cluster configuration
	 */
	public static String ZOOKEEPER_CLUSTER_ADDRESS = "192.168.113.60:2181,192.168.113.61:2181,192.168.113.62:2181,192.168.113.63:2181";
	
	/**
	 * MPI cluster configuration
	 */
	public static String MPI_URL = "http://192.168.114.24:12345/ltp";
	
	/**
	 * 是否开启调试模式
	 */
	public static boolean	DEBUG_MODE					= false;
	/**
	 * 日志输出到kafka
	 */
	public static boolean DEBUG_LOG_TO_KAFKA = false;
	/**
	 * 日志topic
	 */
	public static String DEBUG_LOG_TOPIC = "spider_weibocarelist_log";
	
	public static String ENCODING = "utf-8";
}
