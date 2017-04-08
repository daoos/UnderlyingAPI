package com.qdcz.spider.dao;
import java.util.Date;

import org.bson.Document;
import org.bson.conversions.Bson;

import com.mongodb.MongoClient;
import com.mongodb.MongoWriteException;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;

/**
 * @brief monogodb操作接口类
 * @author hzy
 *
 */
public final class AccountPersistentMap {
	private static final String DEFAULT_HOST = "172.16.40.166";
	private static final String DEFAULT_DATABASE = "weibo_account";
	private static int DEFAULT_PORT = 27017;
	private static final String DEFAULT_COLLECTION = "account_timestamp_map";
	
	private MongoClient client = null;
	private MongoDatabase db = null;
	MongoCollection<Document> collection = null;
	
	private String host="",databaseName="",collectionName="";
	private int port = 0;
	
	public AccountPersistentMap() {
		client = new MongoClient(DEFAULT_HOST, DEFAULT_PORT);
		db = client.getDatabase(DEFAULT_DATABASE);
		collection = db.getCollection(DEFAULT_COLLECTION);
		
		this.port = DEFAULT_PORT;
		this.host = DEFAULT_HOST;
		this.databaseName = DEFAULT_DATABASE;
		this.collectionName = DEFAULT_COLLECTION;
	}
	
	public AccountPersistentMap(String host, int port, 
			String databaseName, String collectionName) 	{
		client = new MongoClient(host, port);
		db = client.getDatabase(databaseName);
		collection = db.getCollection(collectionName);
		
		this.port = port;
		this.host = host;
		this.databaseName = databaseName;
		this.collectionName = collectionName;
	}
	
	private void ReConnect(){
		client = new MongoClient(host, port);
		db = client.getDatabase(databaseName);
		collection = db.getCollection(collectionName);
	}
	
	private Date try_get(String url){
		 Bson query = new Document("url", url);
		 Document d = collection.find(query).first();
		 if(d == null)
			return null;
		 return d.getDate("lastScan");
	}
	
	public Date get(String url) {
		try{
			 return try_get(url);
		}catch(Exception ex){
			ReConnect();
			return try_get(url);
		}
	}
	
//     public Date get(String url) {
//		JudgeAndreConnect();
//		Bson query = new Document("url", url);
//		Document d = collection.find(query).first();
//		if(d == null)
//			return null;
//		
//		Date result = (Date)(d.getDate("lastScan").clone());
//		return result;
//	}
	
	public Date tryGetAndUpdate(String url,Date date,JudgeFlag isNew) {
		

		Bson query = new Document("url", url);
		Document d = collection.find(query).first();
		
		if( d == null){
			put( url,  date);
			isNew.flag = true;
		    return date;	
		}
		Date result = (Date)(d.getDate("lastScan").clone());
		return result;
	}
	
    public Date getAndUpdate(String url,Date date,JudgeFlag isNew) {
    	try{
			 return tryGetAndUpdate(url,date,isNew);
		}catch(Exception ex){
			ReConnect();
			return tryGetAndUpdate(url,date,isNew);
		}
	}
    
    public void put(String url,Date date){
    	
    	try{
			  tryPut(url,date);
		}catch(Exception ex){
			 ReConnect();
			 tryPut(url,date);
		}
    }
    
	
	public void tryPut(String url, Date date) {

		Bson query =  new Document("url", url);
		Bson update = new Document("$set", new Document("lastScan", date));
		UpdateOptions options = new UpdateOptions();
		
		options.upsert(true);
		collection.updateOne(query, update, options);
	}
	
	
	public void put(String id,String type,String posttime,String content,String other){
		
		try{
			 tryPut(id, type, posttime, content, other);
		}catch(Exception ex){
			 ReConnect();
			 tryPut(id,type, posttime, content, other);
		}
	}
	
	public void tryPut(String id,String type,String posttime,String content,String other) {
		Document d = new Document();
		
		d.put("_id", id);
		d.put("type", type);
		d.put("posttime", posttime);
		d.put("content", content);
		d.put("other", other);
		try{
		  collection.insertOne(d);
		}catch(MongoWriteException mrex){
			System.out.println("duplicate key " + mrex.getMessage());
		}
		
	}
	
	public void close(){
		client.close();
	}
	
	public MongoCursor<Document> iterator() {
		return collection.find(new Document()).iterator();
	}
}

