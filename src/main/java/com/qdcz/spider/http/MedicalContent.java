package com.qdcz.spider.http;

public class MedicalContent {

	public int code;
	public byte[] content;
    public String location;
    public int protocolStatusCode;
    public int content_length;
    public String encode;

    public int max_length = 3 * 1024 * 1024;
	public String contentEncoding;
    
    public MedicalContent(){
    	content = new byte[max_length];
    }
    
    public MedicalContent(int p_max){
    	max_length = p_max;
    	content = new byte[max_length];
    	System.out.println(max_length);
    }
	
	public void setCode(int code){
		this.code = code;
	}
	
	public void setmax_length(int length){
		this.max_length = length;
		this.content = new byte[this.max_length]; 
	}
	
	public void setContent(byte[] content){
		this.content_length = Math.min(content.length,max_length);
		System.arraycopy(content, 0, this.content, 0, this.content_length);
	}

	public void setDirectUrl(String location,int protocolStatusCode){
		this.protocolStatusCode = protocolStatusCode;
		this.location = location;
	}
	
	public String getDirectUrl(){
		return this.location;
	}
}
