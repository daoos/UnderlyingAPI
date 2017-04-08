package com.qdcz.spider.http;

import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * 构建HTTP请求包
 */
public class HttpRequestPackageBuilder {
	private String protocol = "HTTP";
	private String protocolVersion = "1.0";
	private String method = "GET";
	private boolean usePostMethod = false;
	private int port = 80;
	private String path = "/";
	private String host = "";
	private String userAgent = "Mozilla/5.0 (Windows NT 10.0; WOW64; rv:41.0) Gecko/20100101 Firefox/41.0";
	private String accept = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
	private String acceptLanguage = "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3";
	private String acceptEncoding = "x-gzip, gzip, deflate";
	private String connection = "close";//默认使用短连接，不然会遇到TCP阻塞的问题
	private String connectType = "application/x-www-form-urlencoded";
	private String postParamsStr = "";
	private String cookie = "";
	private String X_CSRF_TOKEN = "";
	private String xRequestWith = "XMLHttpRequest";
	private String referer = "";
	private int connectLength = 0;
	private boolean useProxy = false;
	private String proxyAddr = "";
	private int proxyPort = 80;
	private StringBuffer requestPackage = null;
	
	private HttpRequestPackageBuilder(){
	}
	
	/**
	 * 支持只传入一个url构造请求包
	 * @param requestStr
	 * @return
	 * @throws MalformedURLException
	 * @throws JSONException
	 */
	public static HttpRequestPackageBuilder getInstance(String requestStr) throws MalformedURLException, JSONException{
		if(requestStr==null || "".equals(requestStr)){
			return null;
		}
		/**
		 * 只允许切分成长度为2的字符串数组，防止因JSON中包含空白字符也被切割导致异常
		 */
		String[] arr = requestStr.split("\\s+", 2);
		if(arr.length>=1){
			return createRequestPackage(arr);
		}else{
			return null;
		}
	}
	
	public static HttpRequestPackageBuilder createRequestPackage(String[] requestArr) throws JSONException, MalformedURLException{
		HttpRequestPackageBuilder builder = new HttpRequestPackageBuilder();
		try {
			URL url = new URL(requestArr[0]);
			builder.host = url.getHost();
			if(url.getPort()!=-1){
				builder.port = url.getPort();
			}
			if(!"".equals(url.getFile())){
				builder.path = url.getFile();
			}
			builder.referer = builder.host;
			if(requestArr.length==2){
				JSONObject json = new JSONObject(requestArr[1]);

				if(json.has("protocol") && !"".equals(json.getString("protocol"))){
					builder.protocol = json.getString("protocol");
				}
				if(json.has("protocolVersion") && !"".equals(json.getString("protocolVersion"))){
					builder.protocolVersion = json.getString("protocolVersion");
				}
				if(json.has("proxyAddr") && json.has("proxyPort")){
					builder.useProxy = true;
					builder.proxyAddr = json.getString("proxyAddr");
					Object portObj = json.get("proxyPort");
					if(portObj instanceof Integer){
						builder.proxyPort = (int)portObj;
					}else if(portObj instanceof String){
						builder.proxyPort = Integer.parseInt((String)portObj);
					}
				}
				if(json.has("method") && !json.isNull("method") && !"".equals(json.getString("method")))
					builder.method = json.getString("method");
				if(json.has("postParams") && !json.isNull("postParams") && !"".equals(json.getString("postParams")))
					builder.postParamsStr = json.getString("postParams");
				
				if(json.has("userAgent") && !json.isNull("userAgent") && !"".equals(json.getString("userAgent")))
					builder.userAgent = json.getString("userAgent");
				if(json.has("accept") && !json.isNull("accept") && !"".equals(json.getString("accept")))
					builder.accept = json.getString("accept");
				if(json.has("acceptLanguage") && !json.isNull("acceptLanguage") && !"".equals(json.getString("acceptLanguage")))
					builder.acceptLanguage = json.getString("acceptLanguage");
				if(json.has("acceptEncoding") && !json.isNull("acceptEncoding") && !"".equals(json.getString("acceptEncoding")))
					builder.acceptEncoding = json.getString("acceptEncoding");
				if(json.has("connection") && !json.isNull("connection") && !"".equals(json.getString("connection")))
					builder.connection = json.getString("connection");
				if(json.has("cookie") && !json.isNull("cookie") && !"".equals(json.getString("cookie")))
					builder.cookie = json.getString("cookie");
				if(json.has("connectType") && !json.isNull("connectType") && !"".equals(json.getString("connectType")))
					builder.connectType = json.getString("connectType");
				if(json.has("connectLength") && !json.isNull("connectLength")){
					Object obj = json.get("connectLength");
					if(obj instanceof Integer){
						builder.connectLength = (int)obj;
					}else if(obj instanceof String){
						builder.connectLength = Integer.parseInt((String)obj);
					}
				}
				
				if(json.has("X_CSRF_TOKEN") && !json.isNull("X_CSRF_TOKEN") && !"".equals(json.getString("X_CSRF_TOKEN")))
					builder.X_CSRF_TOKEN = json.getString("X_CSRF_TOKEN");
				
				if (json.has("x-requested-with") && !json.isNull("x-requested-with") && !"".equals(json.getString("x-requested-with"))) {
					builder.xRequestWith  = json.getString("x-requested-with");
				}
				if (json.has("referer") && !json.isNull("referer") && !"".equals(json.getString("referer"))) {
					builder.referer  = json.getString("referer");
				}
			}
			 builder.createRequestPackage();
			 return builder;
		}catch (JSONException e) {
			throw e;
		}catch (MalformedURLException e) {
			throw e;
		}
	}

	private void createRequestPackage(){
		if("GET".equalsIgnoreCase(this.method)){
			createGETRequestPackage();
		}
		else if("POST".equalsIgnoreCase(this.method)){
			this.usePostMethod = true;
			createPOSTRequestPackage();
		}
	}
	
	private void createGETRequestPackage(){
		createCommonRequestPackage();
		this.requestPackage.append("\r\n");
	}
	
	private void createPOSTRequestPackage(){
		createCommonRequestPackage();
		if(!"".equals(this.xRequestWith))
		this.requestPackage.append("X-Requested-With: ").append(this.xRequestWith).append("\r\n");
		/**
		 * 编码存在一定的问题，暂时取消
		 */
//		try {
//			this.postParamsStr = java.net.URLEncoder.encode(this.postParamsStr,MyConfig.ENCODING);
//			this.connectLength = this.postParamsStr.length();
//		} catch (UnsupportedEncodingException e) {
//			MyLog.trace(this.getClass(), e);
//		} 
		this.connectLength = this.postParamsStr.length();
		if(this.connectLength>=0)
			this.requestPackage.append("Content-Length: ").append(this.connectLength).append("\r\n");
		if(!"".equals(this.connectType))
			this.requestPackage.append("Content-Type: ").append(this.connectType).append("\r\n");
		if(!"".equals(this.postParamsStr)){
			this.requestPackage.append("\r\n");
			this.requestPackage.append(this.postParamsStr+"\r\n");
		}
		this.requestPackage.append("\r\n");
	}
	
	private void createCommonRequestPackage(){
		this.requestPackage = new StringBuffer();
		this.requestPackage.append(this.method).append(" ").append(this.path).append(" "+this.protocol+"/"+this.protocolVersion+"\r\n");
		if(this.port==80){
			this.requestPackage.append("Host: ").append(this.host).append("\r\n");
		}else{
			this.requestPackage.append("Host: ").append(this.host+":"+this.port).append("\r\n");
		}
		this.requestPackage.append("User-Agent: ").append(this.userAgent).append("\r\n");
		this.requestPackage.append("Accept: ").append(this.accept).append("\r\n");
		this.requestPackage.append("Accept-Encoding: ").append(this.acceptEncoding).append("\r\n");
		this.requestPackage.append("Accept-Language: ").append(this.acceptLanguage).append("\r\n");

		
		/**
		 * 该属性建议为空，在HTTP1.1中，会默认保持连接，从而导致读取流阻塞的问题，除非显式指定Connection: Close
		 */
		
		if(!"".equals(this.connection))
			this.requestPackage.append("Connection: ").append(this.connection).append("\r\n");
		this.requestPackage.append("Referer: ").append(this.referer).append("\r\n");
		if(!"".equals(this.cookie))
			this.requestPackage.append("Cookie: ").append(this.cookie).append("\r\n");
		if(!"".equals(this.X_CSRF_TOKEN))
		this.requestPackage.append("X-CSRF-TOKEN: ").append(this.X_CSRF_TOKEN).append("\r\n");
		
	}
	
	public boolean useProxy(){
		return this.useProxy;
	}
	
	public String getProxyAddr(){
		return this.proxyAddr;
	}
	
	public int getProxyPort(){
		return this.proxyPort;
	}
	
	public boolean usePostMethod(){
		return this.usePostMethod;
	}
	
	public StringBuffer getRequestPackage(){
		return this.requestPackage;
	}
	
	public String getHost(){
		return this.host;
	}
	
	public int getPort(){
		return this.port;
	}
}
