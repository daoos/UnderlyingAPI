package com.qdcz.spider.urlconn;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import com.qdcz.spider.http.GZIPUtils;

public class DownloadFunction {
	private Response downBYConn(Request request){
	
		byte[] data = null;
		OutputStream outStream = null;
		HttpURLConnection conn = null;
		InputStream is = null;
		ByteArrayOutputStream swapStream =null;
		Response response = new Response();
		try {
			URL url = null;
			url = new URL(request.getUrl());
			
			//根据代理判断获取连接
			if(request.isUse_proxy()){
				Proxy proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(request.getProxy_host(), request.getProxy_port()));
				conn = (HttpURLConnection) url.openConnection(proxy);
				
			}else{
				conn = (HttpURLConnection) url.openConnection();
			}
			//设置超时时间
			conn.setConnectTimeout(request.getConn_time_out());
			conn.setReadTimeout(request.getRead_time_out());
			conn.setDoOutput(true);
			conn.setRequestMethod(request.getType().toString());
			
			//设置请求头
			for (String key : request.getHeader().keySet()) {
				conn.setRequestProperty(key, request.getHeader().get(key).toString());
			}
			
			//写入post参数流
			if (!request.getParam().isEmpty()) {
				byte[] entity = request.getParam().getBytes();
				outStream = conn.getOutputStream();
				outStream.write(entity);
			}
			//获取状态码
			int resultCode=0;
			resultCode = conn.getResponseCode();
			response.setResult_code(resultCode);

			
			//获取请求头
			response.setHeader(conn.getHeaderFields());

			response.setUrl(conn.getURL().toString());
			
			//获取数据
			is = conn.getInputStream();
			swapStream = new ByteArrayOutputStream();
			byte[] buff = new byte[16384];
			int rc;
			if(is!=null)			
			while ((rc = is.read(buff, 0, buff.length)) != -1) {
				swapStream.write(buff, 0, rc);
			}
			swapStream.flush();
			data = swapStream.toByteArray();

			String Contentecoding = conn.getHeaderField("Content-Encoding");
			if ("gzip".equals(Contentecoding)) {
				data = GZIPUtils.unzipBestEffort(data);
			}

			response.setData(data);
		} catch (Exception e) {
			response.setException(e);
		}finally{
			try {
				if(outStream!=null)
					outStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if(is!=null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(swapStream!=null)
					swapStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if(conn!=null)
					conn.disconnect();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return response;
	}
	
	
	
	public Response downloadByUrlConn(Request request) {
		if (request.getUrl().startsWith("http://")) {
			return downBYConn(request);
		} else if (request.getUrl().startsWith("https://")) {
			return downBYConn(request);
		}
		return null;
	}

	@Deprecated
	public String down_http_get_redirect(String url)
			throws Exception {
		//TODO
		return null;
	}
}
