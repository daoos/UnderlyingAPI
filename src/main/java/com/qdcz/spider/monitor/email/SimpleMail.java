package com.qdcz.spider.monitor.email;

import java.util.List;

public class SimpleMail {
	private String subject;
	private String content;
	private List<String> receiver;
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public List<String> getReceiver() {
		return receiver;
	}
	public void setReceiver(List<String> receiver) {
		this.receiver = receiver;
	}
//	public List<String> getDefaultReceiver(){
//		List<String> recipients = new ArrayList<String>();
//       recipients.add("727608039@qq.com");
//       recipients.add("1162916411@qq.com");
//        recipients.add("18390219567@163.com");
//		recipients.add("892862502@qq.com");
//		if(recipients!=null && !recipients.isEmpty()){
//			return recipients;
//		}
//		return null;
//	}
}
