package com.qdcz.spider.plugin;

import java.util.Vector;

import org.json.JSONObject;

public class ParseResult {
	
	public JSONObject dataJson;// 存储返回的结果串
	public Vector<String> urls;//存储解析得到的新地址
	public Vector<String> needRedirectUrls; //存储需要重定向的地址
	public String currentUrl;//当前需要解析的文档对应的网址
	public byte[] htmlContent;//文档内容
	public boolean urlMatched;//用于judge()方法判断，传递过来的网址是否和本类解析的地址对应
	public boolean isSucceed;//存储解析结果
	public String charset;
	public String nextPageUrl;
	public int totalPage;
	public String searchWord;
	//数据库配置信息
	public String dbHost;//mysql数据库服务器的主机地址
	public String dbPort;//mysql数据库服务器的连接端口
	public String dbName;//mysql数据库服务器的数据库名称
	public String dbCharset;//mysql数据库服务器的数据库编码
	public String dbUser;//mysql数据库服务器的连接用户名
	public String dbPassword;//mysql数据库服务器的连接密码
	
	//mongodb数据库配置信息
	
	
	//需要增加两个构造函数，一个是带mysql信息，一个是带mongodb的信息
	
	
	public ParseResult(String url,byte[] htmlContent,String charset){
		this.urls = new Vector<String>();
		this.dataJson = new JSONObject();
		this.currentUrl = url;
		this.htmlContent = htmlContent;
		this.charset = charset;
		this.urlMatched = false;
		this.isSucceed = false;
	}
	
	public void reset(String url,byte[] htmlContent,String charset){
		this.currentUrl = url;
		this.htmlContent = htmlContent;
		this.charset = charset;
	}
}