package com.qdcz.spider.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

import org.json.JSONException;

import com.qdcz.spider.proxy.MyProxyAuthenticator;

public class Https extends Thread {
	
     private static final int RESULT_OK = 200;
     private boolean open_proxy = false;
     private String proxy_ip;
     private int proxy_port;
     private String cookies = null;
     public void set_proxy(boolean open_proxy,String ip,int port){
    	 
    	 this.open_proxy = open_proxy;
    	 this.proxy_ip = ip;
    	 this.proxy_port = port;
    	 
     }
     
     //设置代理身份验证
//     private String userName = "";
//     private String password = "";
//     public void setProxyAuthenticator(String userName,String password){
//    	 this.userName = userName;
//    	 this.password = password;
//     }
     
     public void setCookies(String cookies){
    	 this.cookies = cookies;
     }
     
     public static void main(String[] args) throws JSONException {

    	 String q = java.net.URLEncoder.encode("\"万里运输\" 血汗钱 OR 欠薪 OR 拖欠工资 OR 上访 OR 群访 OR 罢工 OR 维权 OR 讨债");

    	 String url;
    	 url = "https://www.google.com.hk/search?site=&source=hp&q="+q+"&oq="+q+"&gs_l=hp.12..0l10.8597.19422.0.23169.13.11.2.0.0.0.437.1119.3-2j1.3.0....0...1c.1.64.hp..8.5.1125.SWBX2fZNicg&bav=on.2,or.&bvm=bv.118443451,d.c2E&fp=1&biw=1600&bih=405&dpr=1&tch=1&ech=1&psi=AkwAV9-rBYP8uASrwKKQBg.1459637213445.3&start=0";
    	
    	  url = "https://www.baidu.com/s?ie=utf-8&f=8&rsv_bp=1&rsv_idx=1&ch=&tn=baidu&bar=&wd=apple&rn=&oq=&rsv_pq=cae4d68600003d3a&rsv_t=e1f1JXV2cXUZq5IyMUXVU3HrJuzBE1LrXu%2ByzxBrQ4YHm%2BOgfJVkHMgZygQ";
    	 
    	// url = "https://www.google.com.hk/search?q=%E7%B2%BE%E4%BF%A1%E5%8C%96%E5%B7%A5";
    	 
    	 //url = "https://www.google.com.hk/search?q=%E7%B2%BE%E7%BB%86%E5%8C%96%E5%B7%A5&safe=strict&biw=1600&bih=369&ei=xPARV6CqCsSY0gT_-7L4Bw&start=10&sa=N&dpr=1&bav=on.2,or.&fp=385acb87e64ee025&tch=1&ech=1&psi=vPARV9C_LIbJ0gTFprLYDg.1460793500809.5";
    	 

    	 url= "https://www.baidu.com?wd=apple";
    	 
    	 MedicalContent mcontent = new MedicalContent();
    	 Https google = new Https();
    	 google.set_proxy(true, "122.193.14.108", 80);

    	  url = "http://tjcredit.gov.cn/platform/saic/baseInfo.json?entId=3d0b81430a0a0946013bb0bbb305ff60&departmentId=scjgw&infoClassId=dj";
    	  
    	  url = "http://www.qixin.com/search?key=国基建设集团&type=enterprise&method=all";
    	  url = "http://www.baidu.com";
//		mcontent = new MedicalContent();
//		google = new Https();
//    	 google.set_proxy(true, "125.90.207.93", 8080);

    	 try {
			google.download(url,mcontent);
		 } catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		 }
    	 
       	 byte[] tmp = new byte[mcontent.content_length];
       	 System.arraycopy(mcontent.content, 0, tmp, 0, mcontent.content_length);
       	 String content = null;
		try {
			content = new String(tmp,"gb2312");
			 System.out.println(content);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
       	 
    	 
     }

     public void download(String url,MedicalContent mcontent) throws Exception {
           down_http(url, mcontent);
           String contentEncoding = mcontent. contentEncoding;
           byte[] result = null;
//           System.out.println(this.proxy_ip);
           if("gzip".equals( contentEncoding) || "x-gzip".equals(contentEncoding )) {
                 try {
					result = processGzipEncoded(mcontent.content, new URL(url));
				} catch (Exception e) {
					e.printStackTrace();
				}
           } else if ( "deflate".equals( contentEncoding)) {
                 try {
					result = processDeflateEncoded( mcontent. content,new URL( url));
				} catch (Exception e) {
					e.printStackTrace();
				}
           }
            
           System.arraycopy(result, 0, mcontent.content, 0, result.length);
           mcontent.content_length = result.length;
           result = null;
     }

     public void GetOneUrls(String strRequest, MedicalContent mcontent) {

           HttpURLConnection conn = null;
            byte[] result = null;
            try {
                URL url = new URL( strRequest);
                
                if(this.open_proxy){
//	                 //设置代理服务器身份验证
                	 Authenticator.setDefault(new MyProxyAuthenticator("zhstc123", "hpre"));
                	 
	                 Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(this.proxy_ip, this.proxy_port)); 
	                 conn = (HttpURLConnection) url.openConnection(proxy);
                }
                else
                    conn = (HttpURLConnection) url.openConnection();
                
                 int timeout = 6000;
                 conn.setReadTimeout( timeout);
                 conn.setConnectTimeout( timeout);
                 conn.setRequestMethod( "GET");
                 //conn.setRequestProperty("Host","www.google.com.hk");
                 conn.setRequestProperty( "Accept-Encoding", "gzip, deflate");
                 conn.setRequestProperty( "User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:45.0) Gecko/20100101 Firefox/45.0");
                 //conn.setRequestProperty( "Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
                 //conn.setRequestProperty( "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
                 //conn.setRequestProperty( "DNT","1");
                 //conn.setRequestProperty( "Referer","https://www.google.com.hk");
                 conn.setRequestProperty( "Cookie",
                		 "BAIDUID=0467B93DFDD86462460B002234F663EE:FG=1; BIDUPSID=F012C35E64B35C21E3BF453EB04848F7; PSTM=1461048419; BD_CK_SAM=1; H_PS_PSSID=19567_1469_17757_18280_19689_19559_15139_12090_10634; BD_UPN=13314352; H_PS_645EC=3e680GQ9pgR30wl9rHn0TL0kWNQQeAGrvS7QcvWJ6SoE5%2FrAaRPtx7mks%2FE");
                 conn.setRequestProperty( "Connection", "Keep-Alive");
                 
//                 if(this.cookies != null && this.cookies.length() > 1){
//                    conn.setRequestProperty("Cookie",this.cookies);
//                    System.out.println("set cookies "+ this.cookies);
//                 }
                 
                 int resultCode = conn.getResponseCode();

                 mcontent.code = resultCode;
                 mcontent.contentEncoding = conn.getContentEncoding();
                 mcontent.protocolStatusCode = resultCode;

                 if ( resultCode == RESULT_OK) {
                      mcontent. protocolStatusCode = ProtocolStatus.SUCCESS ;
                     InputStream is = conn.getInputStream();
                     ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                      // I don't handler chunk now,please add.

                      int nRead;
                      byte[] data = new byte[16384];

                      while (( nRead = is.read( data, 0, data. length)) != -1) {
                            buffer.write( data, 0, nRead);
                     }

                      buffer.flush();
                      is.close();
                      result = buffer.toByteArray();

                      int current_length = result. length > mcontent. content. length ? mcontent. content. length
                                : result. length;
                     System. arraycopy(result, 0, mcontent. content, 0, current_length);

                } else if ( resultCode == 410) { // page is gone
                      mcontent. protocolStatusCode = ProtocolStatus.GONE;
                     System. out.println( "Page is gone");
                } else if ( resultCode >= 300 && resultCode < 400) { // handle
                                                                                       // redirect
                     String location = conn.getHeaderField( "Location");
                      // some broken servers, such as MS IIS, use lowercase header
                      // name...
                      if ( location == null)
                            location = conn.getHeaderField( "location");
                      if ( location == null)
                            location = "";
                      mcontent. location = location;

                      int protocolStatusCode;
                      switch ( resultCode) {
                      case 300: // multiple choices, preferred value in Location
                            protocolStatusCode = ProtocolStatus.MOVED;
                            break;
                      case 301: // moved permanently
                      case 305: // use proxy (Location is URL of proxy)
                            protocolStatusCode = ProtocolStatus.MOVED;
                            break;
                      case 302: // found (temporarily moved)
                      case 303: // see other (redirect after POST)
                      case 307: // temporary redirect
                            protocolStatusCode = ProtocolStatus.TEMP_MOVED ;
                            break;
                      case 304: // not modified
                            protocolStatusCode = ProtocolStatus.NOTMODIFIED ;
                            break;
                      default:
                            protocolStatusCode = ProtocolStatus.MOVED;
                     }
                      // handle this in the higher layer.
                      mcontent.setDirectUrl( location, protocolStatusCode);
                } else if ( resultCode == 400) { // bad request, mark as GONE
                      mcontent. protocolStatusCode = ProtocolStatus.GONE;
                     System. out.println( "400 Bad request: " + strRequest);
                } else if ( resultCode == 401) { // requires authorization, but no
                                                            // valid auth provided.
                      mcontent. protocolStatusCode = ProtocolStatus.ACCESS_DENIED ;
                     System. out.println( "401 Authentication Required");
                } else if ( resultCode == 404) {
                      mcontent. protocolStatusCode = ProtocolStatus.NOTFOUND ;
                     System. out.println( "404 Not Found");
                } else if ( resultCode == 410) { // permanently GONE
                      mcontent. protocolStatusCode = ProtocolStatus.GONE;
                     System. out.println( "410 permanently gone");
                } else if(resultCode == 503) {
                      mcontent. protocolStatusCode = 503 ;
                     System. out.println( "Http code=" + resultCode + ", url="
                                + strRequest);
                     
                     
                     InputStream is = conn.getInputStream();
                     ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                      // I don't handler chunk now,please add.

                      int nRead;
                      byte[] data = new byte[16384];

                      while (( nRead = is.read( data, 0, data. length)) != -1) {
                            buffer.write( data, 0, nRead);
                     }

                      buffer.flush();
                      is.close();
                      result = buffer.toByteArray();

                      int current_length = result. length > mcontent. content. length ? mcontent. content. length
                                : result. length;
                     System. arraycopy(result, 0, mcontent. content, 0, current_length);
                     
                     System.out.println(new String(result));
                }

           } catch (MalformedURLException e) {
                 e.printStackTrace();
           } catch (IOException e) {
                 e.printStackTrace();
           } finally {
                 conn.disconnect();
           }
     }// main

     public static byte[] processGzipEncoded( byte[] compressed, URL url)
                 throws IOException {

            //System.out.println("uncompressing....");

            byte[] content;
            content = GZIPUtils. unzipBestEffort(compressed, compressed. length);

            if ( content == null)
                 throw new IOException( "unzipBestEffort returned null");

            /*
           System.out.println("fetched " + compressed.length
                     + " bytes of compressed content (expanded to " + content.length
                     + " bytes) from " + url);
                     */
            return content;
     }

     public byte[] processDeflateEncoded( byte[] compressed, URL url)
                 throws IOException {

            //System.out.println("inflating....");

            byte[] content = DeflateUtils.inflateBestEffort(compressed,
                      compressed. length);

            if ( content == null)
                 throw new IOException( "inflateBestEffort returned null");

            /*
           System.out.println("fetched " + compressed.length
                     + " bytes of compressed content (expanded to " + content.length
                     + " bytes) from " + url);
                     */
            return content;
     }
     
     private void down_http(String url, MedicalContent mcontent) {
            int maxRedirect = 3;
            int maxRetry = 3;

            try {
                 if ( url == null) {
                     System. out.println( "url is null");
                }

                 try {
                      boolean redirecting;
                      boolean retrying;
                      int redirectCount = 0;
                      int retryCount = 0;
                      do {

                            // System.out.println("redirectCount= " + redirectCount);
                            // System.out.println("retryingCount= "+retryCount);
                            redirecting = false;
                            retrying = false;

                           GetOneUrls( url, mcontent);

                            switch ( mcontent. protocolStatusCode) {

                            case ProtocolStatus. SUCCESS: {
                                 return;
                           }
                            case ProtocolStatus. MOVED: // redirect
                            case ProtocolStatus. TEMP_MOVED:
                                 int code;
                                String newUrl = mcontent. location;
                                 if ( newUrl != null && !newUrl.equalsIgnoreCase( "")
                                           && !newUrl.equals( url.toString())) {
                                      url = newUrl;
                                      if ( maxRedirect > 0) {
                                            redirecting = true;
                                            redirectCount++;
                                           System. out.println( " - protocol redirect to "
                                                     + url + " (fetching now)");

                                     } else {
                                           System. out
                                                     .println( " - protocol redirect to "
                                                                + url
                                                                + " (fetching later) because maxRedirect <= 0" );
                                     }
                                } else
                                     System. out.println( " - protocol redirect skipped: "
                                                + (newUrl != null ? "to same url"
                                                           : "filtered"));
                                 newUrl = null;
                                 break;

                            // failures - increase the retry counter
                            case ProtocolStatus. ERROR:
                            case ProtocolStatus. EXCEPTION:
                                 /* FALLTHROUGH */
                            case ProtocolStatus. RETRY: // retry
                                 /* FALLTHROUGH */
                                 // intermittent blocking - retry without increasing
                                 // the counter
                            case ProtocolStatus. WOULDBLOCK:
                            case ProtocolStatus. BLOCKED:
                            case ProtocolStatus. UNKNOWN:
                                 retrying = true;
                                 retryCount++;
                                 // System.out.println("retryCount "+retryCount);
                                 break;

                            // permanent failures
                            case ProtocolStatus. GONE: // gone
                            case ProtocolStatus. NOTFOUND:
                            case ProtocolStatus. ACCESS_DENIED:
                            case ProtocolStatus. ROBOTS_DENIED:
                            case ProtocolStatus. NOTMODIFIED:
                                 return;

                            default:
                                System. out.println( "Unknown ProtocolStatus: "
                                           + mcontent. protocolStatusCode);
                                 retrying = true;
                                 retryCount++;
                                System. out.println( "retryCount " + retryCount);
                           }

                            if ( redirecting && redirectCount >= maxRedirect)
                                System. out.println( " - redirect count exceeded " + url);

                            if ( retryCount >= maxRetry)
                                System. out.println( "retry count exceeded " + url);

                     } while (( redirecting && ( redirectCount < maxRedirect))
                                || ( retrying && ( retryCount < maxRetry)));

                } catch (Throwable t) { // unexpected exception
                     System. out.println( t.getMessage());
                }

           } catch (Exception ex) {
                System. out.println( ex.getMessage());
           }
     }
}