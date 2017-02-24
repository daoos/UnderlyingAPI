package com.qdcz.spider.monitor.email;

import java.util.ArrayList;
import java.util.List;

public class EmailMonitor {
	public static void sendEmail(String title,String content,String receiver){
		List<String> receivers=new ArrayList<String>();
		receivers.add(receiver);
		sendEmail(title,content,receivers);
	}
	
	public static void sendEmail(String title,String content,List<String> receivers){
		
		SimpleMail mail = new SimpleMail();
		mail.setSubject(title);
		mail.setContent(content);
		mail.setReceiver(receivers);
		ProductPriceObserver instance = new ProductPriceObserver();
		instance.update(null, mail);
	}
}
