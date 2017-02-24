package com.qdcz.spider.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class HttpPost {
	public static void main(String args[]) throws Exception{
		StringBuffer requestStr = new StringBuffer();
		requestStr.append("POST /ClientServer.asmx HTTP/1.1\r\n");
		requestStr.append("Content-Type: text/xml; charset=utf-8\r\n");
		requestStr.append("SOAPAction: \"http://www.guitarworld.com.cn/DownloadFile\"\r\n");
		requestStr.append("Host: client.guitarworld.com.cn:8081\r\n");
		requestStr.append("Content-Length: 403\r\n");
		requestStr.append("Expect: 100-continue\r\n");
		requestStr.append("Accept-Encoding: gzip, deflate\r\n");
		requestStr.append("\r\n");
		requestStr.append("<s:Envelope xmlns:s=\"http://schemas.xmlsoap.org/soap/envelope/\"><s:Body xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"><DownloadFile xmlns=\"http://www.guitarworld.com.cn/\"><fileName>D:\\ComsenzEXP\\wwwroot\\bbs\\userspace\\uploadfiles\\qupupic\\8624\\1400506882100.gif</fileName><key>5074e03c6bad5b812b54337b0841e156</key></DownloadFile></s:Body></s:Envelope>\r\n");
		
		Socket socket = null;

	    try {
	      socket = new Socket();                    // create the socket
	      socket.setSoTimeout(10000);

	      //设置socket连接，判断是否使用代理
	      String sockHost = "115.28.178.16";
	      int sockPort = 8081;
	      InetSocketAddress sockAddr= new InetSocketAddress(sockHost, sockPort);
	      socket.connect(sockAddr, 10000);

	      // make request
	      OutputStream req = socket.getOutputStream();

	      byte[] reqBytes= requestStr.toString().getBytes();
	      req.write(reqBytes);
	      req.flush();
	      byte[] buffer = new byte[1024];
	      InputStream in = socket.getInputStream();
	      int size = 0;
	      OutputStream outStream = new FileOutputStream(new File("G:/1.gif"));
	      int total = 0;
	      while((size=in.read(buffer))!=-1){
	    	  System.out.println(size);
	    	  outStream.write(buffer, 0, size);
			  outStream.flush();
			  total += size;
	      }
	      System.out.println("total:"+total);
		  outStream.close();
		  in.close();
	    }catch(Exception e){
	    	e.printStackTrace();
	    }
	}
}
