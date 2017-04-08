package com.qdcz.spider.dao;

import org.apache.commons.lang3.StringUtils;
import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

public final class MongoUtils {
	public static final String DEFAULT_HOST = "instance-00000306";
	public static final String DEFAULT_DATABASE = "forward-chain";
	public static final int DEFAULT_PORT = 27017;
	
	private static MongoClient mongoClient = null;
	private static MongoDatabase db = null;
	
	public static void initial() {
		mongoClient = new MongoClient(DEFAULT_HOST, 
				DEFAULT_PORT);
		db = mongoClient.getDatabase(DEFAULT_DATABASE);
	}
	
	public static void initial(String host, int port, String database) {
		mongoClient = new MongoClient(host, port);
		db = mongoClient.getDatabase(database);
	}
	
	public static MongoDatabase getDatabase() {
		return db;
	}
	
	public static MongoCollection<Document> 
	getCollection(String collectionName) {
		return db.getCollection(collectionName);
	}
	
	public static String nest(String... nest) {
		return StringUtils.join(nest, ".");
	}
	
	public static void MongoCancel() {
		mongoClient.close();
	}
	
	public static <T> boolean find(MongoCollection<T> collection, Bson query) {
		MongoCursor<T> cursor = collection.find(query).iterator();
		if (cursor.hasNext()) 
			return true;
		return false;
	}
	
	public static <T> T get(MongoCollection<T> collection, Bson query) {
		 return collection.find(query).first();
	}
}
