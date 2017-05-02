package com.qdcz.spider.utils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.codecs.blocktree.FieldReader;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.qdcz.spider.http.GZIPUtils;
import com.qdcz.spider.http.MedicalContent;
import com.qdcz.spider.http.UserAgent;
import org.openqa.selenium.support.FindAll;

public class Function {
	/**
	 * 运行定时任务
	 * @param service
	 * @param time
	 * @param call
	 * @return
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public static Object runTimerTask(ExecutorService service,long time,Callable<Object> call) throws InterruptedException, ExecutionException, TimeoutException{
		FutureTask<Object> future = new FutureTask<Object>(call);  
    	service.submit(future);
    	return future.get(time, TimeUnit.MILLISECONDS);
	}
	
	
	
	/**
	 * 多线程的使用方法
	 * 
	 * @param THREAD_NUM
	 * @param call
	 * @return
	 */
	public static List<Object> RunThread(int THREAD_NUM, Callable<Object> call,
			boolean collection) {
		List<Future<Object>> list = new ArrayList<Future<Object>>();
		ExecutorService execs = Executors.newFixedThreadPool(THREAD_NUM);
		for (int i = 0; i < THREAD_NUM; i++) {
			list.add(execs.submit(call));
		}
		List<Object> result = new ArrayList<Object>();
		for (Future<Object> f : list) {
			try {
				if (collection)
					result.add(f.get());
				else
					f.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}
		execs.shutdownNow();
		execs.shutdown();
		System.out.println("搜索-----多线程结束-------线程数：- " + THREAD_NUM);
		return result;
	}

	public static void printFile(byte[] image, String outPath) {
		FileOutputStream fout = null;
		DataOutputStream dout = null;
		try {
			fout = new FileOutputStream(outPath);
			dout = new DataOutputStream(fout);
			dout.write(image);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (fout != null)
					fout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (dout != null)
					dout.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void printFile(String str, String outPath, Boolean append) {

		try {
			FileOutputStream fileOutputStream = new FileOutputStream(outPath,
					append);
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
					fileOutputStream);
			BufferedWriter bufferedWriter = new BufferedWriter(
					outputStreamWriter);
			bufferedWriter.write(str);
			bufferedWriter.close();
			outputStreamWriter.close();
			fileOutputStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 需要cookie
	 * 
	 * @param urlstr
	 * @param cookie
	 * @return
	 * @throws Exception
	 */
	public static byte[] downloadByURLConn(String urlstr, String cookie,
			RequestType type) {
		Map<String, String> key_value = new HashMap<String, String>();
		key_value.put(ConnPropertyType.Cookie, cookie);
		return downloadByURLConn(urlstr, key_value, type);
	}

	/**
	 * 
	 * @param urlstr
	 * @return
	 * @throws Exception
	 */
	public static byte[] downloadByURLConn(String urlstr, RequestType type) {
		return downloadByURLConn(urlstr, new HashMap<String, String>(), type);
	}

	public static interface ConnPropertyType {
		String Accept_Encoding = "Accept-Encoding";
		String Accept_Language = "Accept-Language";
		String Host = "Host";
		String Accept = "Accept";
		String Cookie = "Cookie";
		String Proxy_Authorization = "Proxy-Authorization";
		String Connection = "Connection";
		String Referer = "Referer";
		String param = "param";
		String proxy = "proxy";
		String X_Requested_With = "X-Requested-With";
		String Content_Type = "Content-Type";
	}

	public static enum RequestType {
		GET, POST
	}

	/**
	 * 需要使用代理，同时带入key—�?�value�?
	 * 
	 * @param urlstr
	 * @param key_value
	 * @return
	 * @throws Exception
	 */
	public static byte[] downloadByURLConn(String urlstr,
			Map<String, String> key_value, RequestType type) {
		if (key_value == null)
			key_value = new HashMap<String, String>();
		URL url = null;
		try {
			url = new URL(urlstr);
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		if (url == null)
			return null;

		byte[] data = null;
		OutputStream outStream = null;
		HttpURLConnection conn = null;
		InputStream is = null;
		ByteArrayOutputStream swapStream = null;

		try {
			if (key_value.containsKey(ConnPropertyType.proxy)) {
				try {
					String proxy_str = key_value.get(ConnPropertyType.proxy);
					System.out.println("使用代理       " + proxy_str);
					String[] arrs = proxy_str.split(":");
					String proxy_ip = arrs[0];
					int proxy_port = Integer.parseInt(arrs[1]);
					Proxy proxy = new Proxy(Proxy.Type.HTTP,
							new InetSocketAddress(proxy_ip, proxy_port));
					conn = (HttpURLConnection) url.openConnection(proxy);
				} catch (Exception e) {
					conn = (HttpURLConnection) url.openConnection();
					e.printStackTrace();
				}
			} else
				conn = (HttpURLConnection) url.openConnection();

			int ConnectTimeout = 6000;
			int ReadTimeout = 6000;
			conn.setConnectTimeout(ConnectTimeout);
			conn.setReadTimeout(ReadTimeout);
			conn.setDoOutput(true);
			conn.setRequestMethod(type.toString());
			conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
			conn.setRequestProperty("User-Agent",
					"Mozilla/5.0 (Windows NT 6.1; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
			conn.setRequestProperty("Accept",
					"text ml,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
			conn.setRequestProperty("Accept-Language",
					"zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
			Set<String> keys = key_value.keySet();
			for (String key : keys) {

				if (ConnPropertyType.proxy.equals(key))
					continue;
				conn.setRequestProperty(key, key_value.get(key));
			}
			if (key_value.containsKey(ConnPropertyType.param)) {
				byte[] entity = key_value.get(ConnPropertyType.param)
						.getBytes();
				outStream = conn.getOutputStream();
				outStream.write(entity);
			}
			int resultCode = 0;
			resultCode = conn.getResponseCode();
			is = conn.getInputStream();
			System.out.println(resultCode + "      " + url.toString());

			swapStream = new ByteArrayOutputStream();
			byte[] buff = new byte[16384];
			int rc;
			if (is != null)
				while ((rc = is.read(buff, 0, buff.length)) != -1) {
					swapStream.write(buff, 0, rc);
				}
			swapStream.flush();
			data = swapStream.toByteArray();

			String Contentecoding = conn.getHeaderField("Content-Encoding");
			if ("gzip".equals(Contentecoding)) {
				data = GZIPUtils.unzipBestEffort(data);
			}

		} catch (Exception e) {
			if (e.getMessage().contains("Read timed out"))
				System.out.println("读取超时！！！！！！！！" + urlstr);
			else if (e.getMessage().contains("Connection refused"))
				System.out.println("拒绝连接！！！！！！！！！！Connection refused" + urlstr);
			else if (e.getMessage().contains("connect timed out"))
				System.out.println("连接超时！！！！！！！！Connection refused" + urlstr);
			else
				e.printStackTrace();
		} finally {
			try {
				if (outStream != null)
					outStream.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				if (is != null)
					is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (swapStream != null)
					swapStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (conn != null)
					conn.disconnect();

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return data;
	}

	public static byte[] download(String url) {
		MedicalContent mcontent = new MedicalContent();
		DownloadFunction downFunction = new DownloadFunction();
		downFunction.download(url, mcontent);
		byte[] usedata = new byte[mcontent.content_length];
		System.arraycopy(mcontent.content, 0, usedata, 0,
				mcontent.content_length);
		try {
			Thread.sleep(0);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return usedata;
	}

	private static void segementSentence(String text,
			Map<Object, Object> words) {
		StringReader reader;
		Analyzer analyzer;
		TokenStream ts_standard;

		try {
			reader = new StringReader(text);
			analyzer = new StandardAnalyzer();
			ts_standard = analyzer.tokenStream("", reader);

			while (ts_standard.incrementToken()) {
				CharTermAttribute ta = ts_standard
						.getAttribute(CharTermAttribute.class);

				String one = ta.toString().toLowerCase().trim();

				if (!words.containsKey(one)) {
					words.put(one, new Integer(1));
				} else {
					Integer count = (Integer) words.get(one);
					words.put(one, new Integer(count.intValue() + 1));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		analyzer = null;
		reader = null;
	}

	public static void main(String[] args) {
		String source = Function.readFileOneTime("E:\\work\\temp.txt");
		System.out.println(source);
	}

	public static boolean if_match_ip(String in) {
		String regEx = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(in);
		while (m.find()) {
			return true;
		}
		return false;
	}

	public static boolean fuzzyCompare(String first, String second) {
		boolean result = true;
		Map<Object, Object> first_words = new HashMap<Object, Object>();
		Map<Object, Object> second_words = new HashMap<Object, Object>();

		segementSentence(first, first_words);
		segementSentence(second, second_words);

		String string_first_map = first_words.toString();
		String string_second_map = second_words.toString();

		if (string_first_map.equalsIgnoreCase(string_second_map))
			result = true;
		else
			result = false;

		return result;
	}

	public static Date getDateBefore(Date d, int day) {
		Calendar now = Calendar.getInstance();
		now.setTime(d);
		now.set(Calendar.DATE, now.get(Calendar.DATE) - day);
		return now.getTime();
	}

	public static String getCookieByPhantomJS(String url) throws Exception {

		DesiredCapabilities caps = new DesiredCapabilities();
		caps.setJavascriptEnabled(true);
		caps.setCapability("takesScreenshot", true);
		caps.setCapability("phantomjs.page.settings.loadImages", false);
		caps.setCapability("phantomjs.page.settings.webSecurityEnabled", false);

		String osType = Function.getOSType().toUpperCase();
		if (osType.equals("WIN_32") || osType.equals("WIN_64")) {
			caps.setCapability("phantomjs.binary.path",
					SpiderProperties.getProperty("phantomjs")
							+ "/phantomjs-1.9.7-windows/phantomjs.exe");
		} else if (osType.equals("LINUX_32")) {
			caps.setCapability("phantomjs.binary.path",
					SpiderProperties.getProperty("phantomjs")
							+ "/phantomjs-1.9.8-linux-i686/bin/phantomjs");
		} else if (osType.equals("LINUX_64")) {
			caps.setCapability("phantomjs.binary.path",
					SpiderProperties.getProperty("phantomjs")
							+ "/phantomjs-1.9.8-linux-x86_64/bin/phantomjs");
		}
		caps.setCapability("phantomjs.page.settings.userAgent",
				UserAgent.agentList[0]);
		PhantomJSDriver driver = new PhantomJSDriver(caps);
		driver.manage().deleteAllCookies();
		// 最大化窗口，用于规避该网站的JS窗口检测
		driver.manage().window().maximize();
		driver.get(url);
		Set<Cookie> cookieset = driver.manage().getCookies();
		boolean cookieRight = false;
		StringBuffer cookieGet = new StringBuffer();
		for (Cookie cookie : cookieset) {
			int p = cookie.toString().indexOf(";");
			if (p > -1) {
				cookieGet.append(cookie.toString().substring(0, p) + "; ");
				cookieRight = true;
			}
		}
		driver.close();
		driver.quit();

		if (cookieRight && cookieGet.length() > 1) {
			return cookieGet.toString();
		} else {
			return getCookieByPhantomJS(url);
		}

	}

	public static String readFileOneTime(String inpath) {

		File file = new File(inpath);
		Long file_length = new Long(file.length());
		byte[] file_buffer = new byte[file_length.intValue()];
		String content = "";

		try {
			FileInputStream in = new FileInputStream(file);
			in.read(file_buffer);
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		try {
			content = new String(file_buffer, "utf8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		return content;
	}

	public static byte[] bitSet2ByteArray(BitSet bitSet) {
		byte[] bytes = new byte[bitSet.size() / 8];
		for (int i = 0; i < bitSet.size(); i++) {
			int index = i / 8;
			int offset = 7 - i % 8;
			bytes[index] |= (bitSet.get(i) ? 1 : 0) << offset;
		}
		return bytes;
	}

	public static BitSet byteArray2BitSet(byte[] bytes) {
		BitSet bitSet = new BitSet(bytes.length * 8);
		int index = 0;
		for (int i = 0; i < bytes.length; i++) {
			for (int j = 7; j >= 0; j--) {
				bitSet.set(index++,
						(bytes[i] & (1 << j)) >> j == 1 ? true : false);
			}
		}
		return bitSet;
	}

	// 获得URL的主域名,如http://www.baidu.com则得到baidu
	public static String getDomainIdentification(String url) {
		String id = "Default";
		try {
			URL u = new URL(url);
			String host = u.getHost();

			String[] items = host.split("\\.");
			if (items[items.length - 2].equalsIgnoreCase("com")
					|| items[items.length - 2].equalsIgnoreCase("org")) {
				if (items.length >= 3)
					id = items[items.length - 3];
			} else {
				id = items[items.length - 2];
			}
		} catch (Exception e) {
		}

		return id;
	}

	public static String matchWithFilter(byte[] data, String url) {
		if (url.startsWith("http://weibo.com")
				|| url.startsWith("http://api.weibo.cn"))
			return "utf-8";
		if (url.startsWith("http://bbs.tianya.cn"))
			return "utf-8";
		if (url.startsWith("http://roll.news.sina.com.cn"))
			return "gbk";
		return matchCharset(data);
	}

	/**
	 * 获取传入网页的源码中的使用的字符编码
	 * 
	 * @param data
	 * @return
	 */
	public static String matchCharset(byte[] data) {
		String str_origin = new String(data);
		return matchCharset(str_origin);
	}

	/**
	 * 获取传入网页的源码中的使用的字符编码
	 * 
	 * @param data
	 * @return
	 */
	public static String matchCharset(String data) {
		String regEx = "charset=[“\"]{0,1}([A-Z\\-a-z0-9]*)[“\"]{0,1}";
		Pattern p = Pattern.compile(regEx);
		String str = new String(data);
		Matcher m = p.matcher(str);
		String charset = "utf-8";
		while (m.find()) {
			if (m.groupCount() >= 1) {
				charset = m.group(1);
			}
			break;
		}

		regEx = null;
		p = null;
		str = null;
		m = null;

		// 判断java是否支持此编码格式 有些网页会乱搞rbk等等

		try {
			if (!Charset.isSupported(charset)) {
				throw new RuntimeException("java不支持此次字符集          " + charset);
			}
		} catch (Exception e) {
			throw new RuntimeException(
					"错误字符" + charset + "     " + e.getMessage());
		}

		return charset;
	}

	/**
	 * regex match tool
	 */
	public static boolean if_match(String in, String regex_patter) {
		String regEx = regex_patter;
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(in);
		while (m.find()) {
			return true;
		}
		return false;
	}

	public static String match_month_day(String in) {
		String regEx = "(\\d+)月(\\d+)日";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(in);
		int i;
		StringBuffer sb = new StringBuffer();
		while (m.find()) {
			for (i = 0; i < m.groupCount(); i++) {
				System.out.println(i + " " + m.group(i + 1));
				sb.append(m.group(i + 1));
				sb.append(" ");
			}
		}
		return sb.toString();
	}

	/**
	 * Unicode编码转换成中文UTF-8
	 * 
	 * @param ori
	 * @return
	 */
	public static String convertUnicode(String ori) {
		char aChar;
		int len = ori.length();
		StringBuffer outBuffer = new StringBuffer(len);
		for (int x = 0; x < len;) {
			aChar = ori.charAt(x++);
			if (aChar == '\\') {
				aChar = ori.charAt(x++);
				if (aChar == 'u') {
					// Read the xxxx
					int value = 0;
					for (int i = 0; i < 4; i++) {
						aChar = ori.charAt(x++);
						switch (aChar) {
							case '0' :
							case '1' :
							case '2' :
							case '3' :
							case '4' :
							case '5' :
							case '6' :
							case '7' :
							case '8' :
							case '9' :
								value = (value << 4) + aChar - '0';
								break;
							case 'a' :
							case 'b' :
							case 'c' :
							case 'd' :
							case 'e' :
							case 'f' :
								value = (value << 4) + 10 + aChar - 'a';
								break;
							case 'A' :
							case 'B' :
							case 'C' :
							case 'D' :
							case 'E' :
							case 'F' :
								value = (value << 4) + 10 + aChar - 'A';
								break;
							default :
								throw new IllegalArgumentException(
										"Malformed   \\uxxxx   encoding.");
						}
					}
					outBuffer.append((char) value);
				} else {
					if (aChar == 't')
						aChar = '\t';
					else if (aChar == 'r')
						aChar = '\r';
					else if (aChar == 'n')
						aChar = '\n';
					else if (aChar == 'f')
						aChar = '\f';
					outBuffer.append(aChar);
				}
			} else
				outBuffer.append(aChar);

		}
		return outBuffer.toString();
	}

	/**
	 * 
	 * @Title: getOSType @Description: 或得操作系统类型 @author qdcz @throws
	 */
	public static String getOSType() {
		String os = System.getProperties().getProperty("os.name");
		if (os.contains("Windows")) {
			String arch = System.getenv("PROCESSOR_ARCHITECTURE");
			String wow64Arch = System.getenv("PROCESSOR_ARCHITEW6432");
			if (arch.endsWith("64")
					|| wow64Arch != null && wow64Arch.endsWith("64")) {
				return "WIN_64";
			} else {
				return "WIN_32";
			}
		} else if (os.contains("Linux")) {
			try {
				Process process = Runtime.getRuntime().exec("getconf LONG_BIT");
				BufferedReader bufferedReader = new BufferedReader(
						new InputStreamReader(process.getInputStream()));
				String s = bufferedReader.readLine();
				if (s.contains("64")) {
					return "Linux_64";
				} else {
					return "Linux_32";
				}
			} catch (IOException e) {
				return "Unknown";
			}
		}
		return "Unknown";
	}

	/**
	 * 将传入的字符串用MD5加密
	 * 
	 * @param sourceStr
	 * @return
	 */
	public static String str2MD5(String sourceStr) {
		String result = "";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(sourceStr.getBytes());
			byte b[] = md.digest();
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			result = buf.toString();
		} catch (NoSuchAlgorithmException e) {
			System.out.println(e);
		}
		return result;
	}
	public static List<String> readFile(String path){
		File file = new File(path);
		Reader fieldReader = null;
		BufferedReader bufReader = null;
		try {
			List<String> allContent = new ArrayList<>();
			fieldReader = new FileReader(file);
			bufReader = new BufferedReader(fieldReader);
			String line = "";
			while ((line = bufReader.readLine())!=null){
				if(line.isEmpty()){
					continue;
				}
				allContent.add(line.trim());

			}

			return allContent;

		} catch (IOException e) {
			e.printStackTrace();
		}finally {

			if(bufReader!=null){
				try {
					bufReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(fieldReader!=null){
				try {
					fieldReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		return null;

	}
}
