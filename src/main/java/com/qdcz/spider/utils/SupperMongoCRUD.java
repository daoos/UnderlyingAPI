package com.qdcz.spider.utils;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

/**
 * mongodb的操作类
 * 
 * @author hpre
 *
 */
public class SupperMongoCRUD {
	/**
	 * mongo节点包含查询时的包含类型枚举
	 * 
	 * @author hpre
	 *
	 */
	public static enum ContainsType {
		left, right, complete, contains
	}

	private String user = "sjcjb";
	private String pass = "hpre&-*123";
	private MongoClient client = null;
	private MongoDatabase db = null;
	private MongoCollection<Document> collection = null;

	private String host = "";
	private String databaseName = "";
	private String collectionName = "";
	private int port = 0;

	/**
	 * 关闭mongo链接
	 */
	public void close() {
		if (this.client != null)
			this.client.close();
	}

	/**
	 * 获取连接的结合名称
	 * 
	 * @return
	 */
	public String getCollectionName() {
		return collectionName;
	}

	public SupperMongoCRUD(String host, String databaseName, int port,
			String collectionName) {
		this.host = host;
		this.databaseName = databaseName;
		this.port = port;
		this.collectionName = collectionName;
		connect();
	}

	public SupperMongoCRUD(String host, String databaseName, int port,
			String collectionName, String user, String pass) {
		this.host = host;
		this.databaseName = databaseName;
		this.port = port;
		this.collectionName = collectionName;
		this.user = user;
		this.pass = pass;
		connect();
	}

	private void connect() {
		MongoCredential credential = MongoCredential.createCredential(user,
				databaseName, pass.toCharArray());
		client = new MongoClient(new ServerAddress(host, port),
				Arrays.asList(credential));
		db = client.getDatabase(databaseName);
		collection = db.getCollection(collectionName);
	}

	/**
	 * 更换连接的mongo集合
	 * 
	 * @param collectionName
	 */
	public void changeCollection(String collectionName) {
		if (!this.collectionName.equals(collectionName)) {
			this.collectionName = collectionName;
			collection = db.getCollection(collectionName);
		}
	}

	/**
	 * 通过节点包含进行搜索,注意：在数据量多的库内搜索的时候必须在所搜索的字段上面做好索引，
	 * 
	 * @param node
	 * @param contains
	 * @param type
	 * @return
	 */
	public synchronized JSONArray getByNodeContains(String node,
			String contains, ContainsType type) {
		JSONArray arr = new JSONArray();
		try {
			Pattern pattern = null;
			switch (type) {
				case complete :
					// 1、完全匹配
					pattern = Pattern.compile("^" + contains + "$",
							Pattern.CASE_INSENSITIVE);
					break;
				case left :
					// 2、左匹配
					pattern = Pattern.compile("^" + contains + ".*$",
							Pattern.CASE_INSENSITIVE);
					break;
				case right :
					// 3、右匹配
					pattern = Pattern.compile("^.*" + contains + "$",
							Pattern.CASE_INSENSITIVE);
					break;
				default :
					// 4、模糊匹配
					pattern = Pattern.compile("^.*" + contains + ".*$",
							Pattern.CASE_INSENSITIVE);
					break;
			}

			BasicDBObject query = new BasicDBObject();
			query.put(node, pattern);
			MongoCursor<Document> list = collection.find(query).iterator();
			while (list.hasNext()) {
				Document doc = list.next();
				try {
					arr.put(new JSONObject(doc.toJson()));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return arr;
	}

	/**
	 * 询问是否存在传入索引
	 * 
	 * @param _id
	 * @return
	 */
	public boolean ask_exist(String _id) {
		Bson query = new Document("_id", _id);
		Document d = null;
		try {
			d = collection.find(query).first();
		} catch (Exception ex) {
			try {
				System.out.println("mongo查询失败，重新查询是否存在：" + _id);
				d = collection.find(query).first();
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		if (d == null)
			return false;
		return true;
	}

	public JSONArray getOnePage(String lastId, int pageSize) {
		// Bson query = new Document("_id",new );
		BasicDBObject query = new BasicDBObject("_id",
				new BasicDBObject(QueryOperators.GT, lastId));
		JSONArray onepage = new JSONArray();
		MongoCursor<Document> limit = null;
		if (lastId.equals("one") || lastId.isEmpty())
			limit = collection.find().skip(0).sort(new BasicDBObject("_id", 1))
					.limit(pageSize).iterator();
		else
			limit = collection.find(query).skip(0)
					.sort(new BasicDBObject("_id", 1)).limit(pageSize)
					.iterator();
		while (limit.hasNext()) {
			Document doc = limit.next();
			try {
				onepage.put(new JSONObject(doc.toJson()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return onepage;
	}

	/**
	 * 按照翻页获取mongo中的数据，首页为第一页
	 * 
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public JSONArray getOnePage(int page, int pageSize) {
		JSONArray onepage = new JSONArray();
		long pagecount = getCollectionCount();
		int total_page = (int) ((pagecount - 1) / pageSize) + 1;
		if (page < 1)
			page = 1;
		else if (page > total_page)
			page = total_page;
		MongoCursor<Document> limit = collection.find()
				.skip((page - 1) * pageSize).sort(new BasicDBObject())
				.limit(pageSize).iterator();
		while (limit.hasNext()) {
			Document doc = limit.next();
			try {
				onepage.put(new JSONObject(doc.toJson()));
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return onepage;
	}

	/**
	 * 获取集合中的记录总计数
	 * 
	 * @return
	 */
	public long getCollectionCount() {
		return collection.count();
	}

	/**
	 * 获取集合中的所有文档,文档多的时候特慢
	 * 
	 * @return
	 */
	@Deprecated
	public JSONArray get_all_documents() {
		JSONArray all = new JSONArray();
		MongoCursor<Document> iterator = collection.find(new Document())
				.iterator();
		while (iterator.hasNext()) {
			Document doc = iterator.next();
			try {
				all.put(new JSONObject(doc.toJson()));
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
		return all;
	}

	public static void main(String[] args) {
		try {
			SupperMongoCRUD mongo = new SupperMongoCRUD("localhost", "test", 27017,
					"platData", "localmongo", "qdcz!@#");
			
		
			JSONObject condition = new JSONObject();
			condition.put("_id", "鑫合汇");
			condition.put("volumes.364.date", "2017-02-15");
			
			System.out.println(mongo.findByJson(condition));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	

	/**
	 * 移除一条记录
	 * 
	 * @param _id
	 */
	public void removeOneDocument(String _id) {
		Bson query = new Document("_id", _id);
		collection.deleteOne(query);
		System.out.println("删除一条记录：" + _id);

	}
	
	public JSONObject findByJson(JSONObject condition){
		JSONObject result = null;
		Bson query = Document.parse(condition.toString());
		
		Document d = null;
		try {
			d = collection.find(query).first();
		} catch (Exception ex) {
			try {
				d = collection.find(query).first();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (d != null) {
			try {
				result = new JSONObject(d.toJson());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 获取一条记录，有则返回，无则返回null
	 * 
	 * @param _id
	 * @return
	 */
	public synchronized JSONObject getOneDocument(String _id) {
		JSONObject result = null;
		Bson query = new Document("_id", _id);
		Document d = null;
		try {
			d = collection.find(query).first();
		} catch (Exception ex) {
			try {
				d = collection.find(query).first();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (d != null) {
			try {
				result = new JSONObject(d.toJson());
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 不存在保存，存在替换一条记录
	 * 
	 * @param _id
	 * @param record
	 * @return
	 */
	public boolean saveOrReplaceOneDocument(String _id, JSONObject record) {
		Document article = Document.parse(record.toString());
		article.put("_id", _id);
		if (saveWhenNotEist(_id, record))
			return true;
		collection.replaceOne(new Document().append("_id", _id), article);
		System.out.println("table:  " + this.collectionName
				+ ": replace one record  ID:" + _id);
		return false;
	}

	/**
	 * 不存在则保存，存在则忽略此次操作
	 * 
	 * @param _id
	 * @param obj
	 * @return
	 */
	public boolean saveWhenNotEist(String _id, JSONObject obj) {
		if (!ask_exist(_id)) {
			Document article = Document.parse(obj.toString());
			article.put("_id", _id);
			collection.insertOne(article);
			System.out.println("table:  " + this.collectionName
					+ " save one record  ID:" + _id);
			return true;
		}
		return false;
	}
	
	/**
	 * 
	    * @Title: setNode  
	    * @Description: 更新一个内嵌数组的字段  | 来指定一个键的值，如果不存在则创建它 
	    * @author qdcz
	    * @throws
	 */
	public void setNode(JSONObject idJson,String arrName,String setKey,String setValue){
		//db.platData.update({"_id":"微贷网","volumes.date":"2017-02-13"},{$set:{"volumes.$.oneVolume":"19022.54"}})
		if (arrName==null || arrName.equals("") || setKey==null 
				|| setKey.equals("") || setValue==null || setValue.equals("")) {
			return;
		}
		Bson _id = Document.parse(idJson.toString());
		
		Document value = new Document().append(arrName + ".$." + setKey, setValue);
		Bson set = new Document("$set",value);
		collection.updateOne(_id, set);
	}

	/**
	 * 
	    * @Title: addNodeToDoc  
	    * @Description: 向文档中的数组  追加一个节点 若此数组不存在则创建此数组并将该节点保存
	    * @author qdcz
	    * @throws
	 */
	public void addNodeToDoc(String docId,String arrName,Object node){
		//db.platData.update({"_id":"陆金所"},{$push:{"volumes":{"date":"2017-02-14","oneVolume":"666666.666666"}}})
		if (docId==null || arrName==null || docId.equals("") || arrName.equals("") || node==null) {
			return;
		}
		Document arr = new Document(arrName,Document.parse(node.toString()));
		
		
		Bson _id = new Document("_id",docId);
		Bson push = new Document("$push",arr);
		collection.updateOne(_id, push);
		
	}
	

	/**
	 * 一个根节点是数组的里面添加元素
	 * 
	 * @param _id
	 * @param key
	 * @param value
	 */
	public void appendToNode(String _id, String key, JSONArray value) {
		Bson query = new Document("_id", _id);
		Document d = null;
		try {
			d = collection.find(query).first();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (d != null) {
			@SuppressWarnings("unchecked")
			ArrayList<JSONObject> arr = (ArrayList<JSONObject>) d.get(key);
			if (arr == null) {
				arr = new ArrayList<JSONObject>();
			}
			for (int i = 0; i < arr.size(); i++) {
				value.put(arr.get(i));
			}
			JSONObject obj = new JSONObject();
			try {
				obj.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			BasicDBObject newdoc = new BasicDBObject(d);
			newdoc.putAll(Document.parse(obj.toString()));
			if (d != null) {
				collection.updateOne(new Document().append("_id", _id),
						new Document("$set", newdoc));
				System.out.println("table:  " + this.collectionName
						+ " append one node:" + key + "  ID:" + _id);
			}
		}
	}

	/**
	 * 保存或者更新节点
	 * 
	 * @param _id
	 * @param key
	 * @param value
	 */
	public void saveOrUpdateNode(String _id, String key, JSONArray value) {
		JSONObject obj = new JSONObject();
		try {
			obj.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (saveWhenNotEist(_id, obj))
			return;
		updateOneNode(_id, key, obj);
	}

	/**
	 * 保存或者更新节点
	 * 
	 * @param _id
	 * @param key
	 * @param value
	 */
	public void saveOrUpdateNode(String _id, String key, JSONObject value) {
		JSONObject obj = new JSONObject();
		try {
			obj.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (saveWhenNotEist(_id, obj))
			return;
		updateOneNode(_id, key, obj);
	}

	/**
	 * 保存或者更新节点
	 * 
	 * @param _id
	 * @param key
	 * @param value
	 */
	public void saveOrUpdateNode(String _id, String key, String value) {
		JSONObject obj = new JSONObject();
		try {
			obj.put(key, value);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if (saveWhenNotEist(_id, obj))
			return;
		updateOneNode(_id, key, obj);
	}

	/**
	 * 更新节点
	 * 
	 * @param _id
	 * @param key
	 * @param obj
	 */
	private void updateOneNode(String _id, String key, JSONObject obj) {
		Bson query = new Document("_id", _id);
		Document d = null;
		try {
			d = collection.find(query).first();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (d != null) {
			BasicDBObject newdoc = new BasicDBObject(d);

			newdoc.putAll(Document.parse(obj.toString()));
			if (d != null) {
				collection.updateOne(new Document().append("_id", _id),
						new Document("$set", newdoc));
				System.out.println("table:  " + this.collectionName
						+ " update one node:" + key + "  ID:" + _id);
			}
		}
	}
}
