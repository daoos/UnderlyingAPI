package com.qdcz.spider.proxy;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

//代理身份
public class MyProxyAuthenticator extends Authenticator {
	String user = "";
	String password = "";

	public MyProxyAuthenticator(String user, String password) {
		super();
		this.user = user;
		this.password = password;
	}
	public PasswordAuthentication getPasswordAuthentication(){
		PasswordAuthentication apa = new PasswordAuthentication(user, password.toCharArray());
		 return apa;
	 }
}
