package com.qdcz.spider.monitor.email;


public class MailSenderFactory {
	private static SimpleMailSender serviceSms = null;
//	private static final String default_username = "1363774964@qq.com";
//	private static final String default_password = "odsxcuvuhwqshjcf";
	private static final String default_username = "b727608039@126.com";
	private static final String default_password = "Bjh524530";
    /**
     * 获取邮箱
     * 
     * @param type 邮箱类型
     * @return 符合类型的邮箱
     */
    public static SimpleMailSender getSender() {
	    serviceSms = new SimpleMailSender(default_username,default_password);
	    return serviceSms;
    }
}
