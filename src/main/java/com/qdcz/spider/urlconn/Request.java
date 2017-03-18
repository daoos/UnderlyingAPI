package com.qdcz.spider.urlconn;

import java.util.HashMap;
import java.util.Map;
/**
 * 
 * @author Star
 *
 */
public class Request {
	public Request() {
		header = new HashMap<String, Object>();
	}
	
	private String url;
	
	//请求方式
	private TYPE type;
	//请求头
	private Map<String, Object> header;
	
	
	//代理设置
	private String proxy_host;//代理
	private int proxy_port;//代理窗口
	private boolean use_proxy = false; //是否使用代理
	private String proxy_user;//代理用户名
	private String proxy_pass;//代理密码
	
	//超时时间
	private int read_time_out = 6000;//读取超时
	private int conn_time_out = 6000;//连接超时
	
	//post参数体
	private String param = "";
	
	/**
	 * 设置代理，不具有用户名和密码
	 * @param proxy_host
	 * @param proxy_port
	 * @param proxy_user
	 * @param proxy_pass
	 */
	public void setProxy(String proxy_host,int proxy_port){
		this.proxy_host = proxy_host;
		this.proxy_port = proxy_port;
		use_proxy = true;
	}
	
	
	/**
	 * 设置代理和用户名密码
	 * @param proxy_host
	 * @param proxy_port
	 * @param proxy_user
	 * @param proxy_pass
	 */
	public void setProxy(String proxy_host,int proxy_port,String proxy_user,String proxy_pass){
		this.proxy_host = proxy_host;
		this.proxy_port = proxy_port;
		use_proxy = true;
		setProxyAuth(proxy_user, proxy_pass);
	}
	
	/**
	 * 设置代理验证用户名和密码
	 * @param proxy_user
	 * @param proxy_pass
	 */
	public void setProxyAuth(String proxy_user,String proxy_pass){
		this.proxy_user = proxy_user;
		this.proxy_pass = proxy_pass;		
	}
	
	public Map<String, Object> getHeader() {
		return header;
	}
	public Object getHeader(String key) {
		return header.get(key);
	}
	
	public void setHeader(String key,Object value){
		header.put(key, value);
	}

	public TYPE getType() {
		return type;
	}

	public void setType(TYPE type) {
		this.type = type;
	}

	public String getProxy_host() {
		return proxy_host;
	}

	public int getProxy_port() {
		return proxy_port;
	}

	public boolean isUse_proxy() {
		return use_proxy;
	}

	public String getProxy_user() {
		return proxy_user;
	}

	public String getProxy_pass() {
		return proxy_pass;
	}

	public int getRead_time_out() {
		return read_time_out;
	}

	public void setRead_time_out(int read_time_out) {
		this.read_time_out = read_time_out;
	}

	public int getConn_time_out() {
		return conn_time_out;
	}

	public void setConn_time_out(int conn_time_out) {
		this.conn_time_out = conn_time_out;
	}

	public String getParam() {
		return param;
	}

	
	public void setParam(String param) {
		this.param = param;
	}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * 请求方式类
	 * @author Star
	 *
	 */
	public static enum TYPE{
		GET,
		POST
	} 
}
