package com.qdcz.spider.urlconn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 响应对象
 * @author Star
 *
 */
public class Response {
	//响应头
	private Map<String, List<String>> header;
	public Response() {
		
	}
	
	
	//状态码
	private int result_code;
	
	//返回数据
	private byte[] data;
	
	private Exception exception;
	
	
	public Map<String, List<String>> getHeader() {
		return header;
	}
	
	public List<String> getHeader(String key) {
		return header.get(key);
	}
	
	public String getHeaderFiled(String key) {
		if(header.get(key).size()==1){
			return header.get(key).get(0);			
		}else{			
			return null;			
		}
	}
	
	public int getResult_code() {
		return result_code;
	}
	
	public void setHeader(Map<String, List<String>> map) {
		this.header = map;
	}

	public void setResult_code(int result_code) {
		this.result_code = result_code;
	}

	public byte[] getData() {
		return data;
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public Exception getException() {
		return exception;
	}

	public void setException(Exception exception) {
		this.exception = exception;
	}
}
