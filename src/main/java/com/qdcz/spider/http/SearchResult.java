package com.qdcz.spider.http;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({"pihao", "place", "postitive", "abstract", "category"})
public class SearchResult implements Comparable<SearchResult> {
	@JsonProperty("time")
	private long 	timestamp;

	@JsonProperty("content")
	private String	content;
	@JsonProperty("title")
	private String	title;
	@JsonProperty("type")
	private String	type;		// 指示数据类型
	private String	pihao;
	private String	place;
	private boolean	postitive; 	// 极性: true 正面，false 负面
	private String	_abstract; 	// 由 content 生成的摘要
	private String 	category;
	private String url;
	private String court;
	
	

	

	public String getCourt() {
		return court;
	}

	public void setCourt(String court) {
		this.court = court;
	}

	private String keyword; // 搜索结果使用的关键词
	
	public SearchResult() {
		
	}
	
	@JsonCreator
	public SearchResult(@JsonProperty("time") long timestamp, 
			@JsonProperty("content") String content, 
			@JsonProperty("title") String title, 
			@JsonProperty("type") String type, 
			@JsonProperty("url") String url,
			@JsonProperty("keyword") String keyword) {
		
		this.timestamp = timestamp;
		this.content = content;
		this.title = title;
		this.type = type;
		this.url = url;
		this.keyword = keyword;
	}
	

	public String getUrl(){
		return this.url;
	}
	
	public void setUrl(String url){
		this.url = url;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getPihao() {
		return pihao;
	}

	public void setPihao(String pihao) {
		this.pihao = pihao;
	}

	public void setPlace(String place) {
		this.place = place;
	}

	public String getPlace() {
		return this.place;
	}

	public int compareTo(SearchResult result) {
		return Long.compare(result.getTimestamp(), this.getTimestamp());
	}

	@Override
	public String toString() {
		return timestamp + "," + category;// + "," + content.substring(0, 10);
	}

	public boolean isPostitive() {
		return postitive;
	}

	public void setPostitive(boolean postitive) {
		this.postitive = postitive;
	}

	public String getAbstract() {
		return _abstract;
	}

	public void setAbstract(String _abstract) {
		this._abstract = _abstract;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}
