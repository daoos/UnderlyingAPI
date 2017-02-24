package com.qdcz.spider.monitor.email;


import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import java.util.*;


public class ProductPriceObserver implements Observer{
	@Override
    public void update(Observable title_obj, Object param) {
		if(param instanceof HashMap){
			HashMap<String,String> map  = (HashMap<String, String>) param;
	        // 发送邮件
	        SimpleMailSender sms = MailSenderFactory
	            .getSender();
	        List<String> recipients = new ArrayList<String>();
	 //       recipients.add("727608039@qq.com");
	        recipients.add("11629164111@qq.com");
	        String title = map.get("title");
	        String content = map.get("content");
	        try {
	            for (String recipient : recipients) {
	            sms.send(recipient, title, content);
	            }
	        } catch (AddressException e) {
	            e.printStackTrace();
	        } catch (MessagingException e) {
	            e.printStackTrace();
	        }
		}else if(param instanceof SimpleMail){
			SimpleMail mail = (SimpleMail) param;
			//发送邮件的账号
			SimpleMailSender sms = MailSenderFactory.getSender();
			List<String> recipients = mail.getReceiver();
			try {
				sms.send(recipients, mail);
			} catch (AddressException e) {
				e.printStackTrace();
			} catch (MessagingException e) {
				e.printStackTrace();
			}
		}
    }
}
