
package com.qdcz.spider.utils;

import com.jayway.jsonpath.JsonPath;
import com.qdcz.spider.http.MedicalContent;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Tools {

    /*
     * Xpath工具 getMultiResultsByOneXpathPatter getResultsByMultiXpathPatter
     */

    public static String get_one_item(byte[] usedata, String charset, String xpath) {

	Vector<String> result = new Vector<String>();

	getMultiResultsByOneXpathPattern(usedata, charset, xpath, result);

	if (result.size() > 0)
	    return result.get(0);
	else
	    return null;

    }

    public static String post_function(DownloadFunction downFunction, MedicalContent mcontent, String baseUrl,
                                       String postString) {

	JSONObject json = new JSONObject();
	try {
	    json.put("method", "POST");
	    json.put("postParams", postString);
	    json.put("userAgent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:47.0) Gecko/20100101 Firefox/47.0");

	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	
	downFunction.download(baseUrl + " " + json.toString(), mcontent);

	byte[] data = new byte[mcontent.content_length];
	System.arraycopy(mcontent.content, 0, data, 0, mcontent.content_length);

	String content = new String(data);

	return content;

    }

    // 从一段字符串中提取出中文串
    public static String Take_chinese_for_str(String str) {
	StringBuffer sb = new StringBuffer();
	StringBuffer sb_all = new StringBuffer();
	for (int i = 0; i < str.length(); i++) {
	    if ((str.charAt(i) + "").getBytes().length > 1) {
		sb.append(str.charAt(i));
	    } else {
		sb_all.append(sb.toString());
		sb = null;
		sb = new StringBuffer();
	    }
	}
	sb_all.append(sb.toString());
	return sb_all.toString();
    }

    // 将 GB2312 编码格式的字符串转换为 UTF-8 格式的字符串：
    public static String gb2312ToUtf8(String str) {

	String urlEncode = "";

	try {

	    urlEncode = URLEncoder.encode(str, "UTF-8");

	} catch (UnsupportedEncodingException e) {

	    e.printStackTrace();

	}
	return urlEncode;
    }

    public static String unicode2String(String unicode) {
	StringBuffer string = new StringBuffer();
	 
	String[] hex = unicode.split("\\\\u");
	 
    	for (int i = 1; i < hex.length; i++) { 
    	    // 转换出每一个代码点
    	    int data = Integer.parseInt(hex[i], 16);
    	    // 追加成string
    	    string.append((char) data);
    	} 
	return string.toString();
    }
    
    public static Vector<String> parserjson(JSONObject json) {
	Vector<String> list = new Vector<String>();
	JSONArray holders = null;
	JSONArray invested = null;
	try {
	    holders = json.getJSONArray("holders");
	    invested = json.getJSONArray("invested");
	    for (int i = 0; i < holders.length(); i++) {
		JSONObject one_holder = holders.getJSONObject(i);
		String one_holdername = one_holder.getString("shareholder");
		if (one_holdername.length() > 5) {
		    list.add(one_holdername);
		}
	    }
	    for (int i = 0; i < invested.length(); i++) {
		JSONObject one_invested = invested.getJSONObject(i);
		String one_investedname = one_invested.getString("name");
		if (one_investedname.length() > 5) {
		    list.add(one_investedname);
		}
	    }
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	return list;
    }

    // 去除字符串中首尾指定字符
    public static String trimFirstAndLastChar(String source, char element) { // 去除字符串中首尾指定字符
	boolean beginIndexFlag = true;
	boolean endIndexFlag = true;
	do {
	    int beginIndex = source.indexOf(element) == 0 ? 1 : 0;
	    int endIndex = source.lastIndexOf(element) + 1 == source.length() ? source.lastIndexOf(element)
		    : source.length();
	    source = source.substring(beginIndex, endIndex);
	    beginIndexFlag = (source.indexOf(element) == 0);
	    endIndexFlag = (source.lastIndexOf(element) + 1 == source.length());
	} while (beginIndexFlag || endIndexFlag);
	return source;
    }

    // 取得域名
    public static String get_domin_name(String url) {
	URL url_object = null;
	try {
	    url_object = new URL(url);
	} catch (MalformedURLException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	String baseurl = null;
	if (url_object != null) {
	    if (url_object.getPort() != -1)// url_object.getPort()=-1
		baseurl = url_object.getProtocol() + "://" + url_object.getHost() + ":" + url_object.getPort();// url_object.getProtocol()=http
	    else
		baseurl = url_object.getProtocol() + "://" + url_object.getHost();// url_object.getHost()=gsxt.scaic.gov.cn
	}
	// System.out.println(url_object.getProtocol()+"NNN"+url_object.getHost()+"NNNN"+url_object.getPort());
	return baseurl;
    }

    public static Map<String, Integer> sortMapByValue(Map<String, Integer> oriMap) {
	Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
	if (oriMap != null && !oriMap.isEmpty()) {
	    List<Entry<String, Integer>> entryList = new ArrayList<Entry<String, Integer>>(oriMap.entrySet());
	    Collections.sort(entryList, new Comparator<Entry<String, Integer>>() {
		public int compare(Entry<String, Integer> entry1, Entry<String, Integer> entry2) {
		    int value1 = 0, value2 = 0;
		    value1 = entry1.getValue();
		    value2 = entry2.getValue();
		    return value2 - value1;
		}

		private int getInt(String value) {
		    Integer it = new Integer(value);
		    int total = it.intValue();
		    return total;
		}
	    });
	    Iterator<Entry<String, Integer>> iter = entryList.iterator();
	    Entry<String, Integer> tmpEntry = null;
	    while (iter.hasNext()) {
		tmpEntry = iter.next();
		sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
	    }
	}
	return sortedMap;
    }

    public static String EncodeUtf8ByteToString(byte[] buffer) {
	int count = 0;
	int index = 0;
	byte a = 0;
	int utfLength = buffer.length;
	char[] result = new char[utfLength];

	while (count < utfLength) {
	    if ((result[index] = (char) buffer[count++]) < 0x80) {
		index++;
	    } else if (((a = (byte) result[index]) & 0xE0) == 0xC0) {
		if (count >= utfLength) {
		    try {
			throw new IOException("Invalid UTF-8 encoding found, start of two byte char found at end.");
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}

		byte b = buffer[count++];
		if ((b & 0xC0) != 0x80) {
		    try {
			throw new IOException("Invalid UTF-8 encoding found, byte two does not start with 0x80.");
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}

		result[index++] = (char) (((a & 0x1F) << 6) | (b & 0x3F));

	    } else if ((a & 0xF0) == 0xE0) {

		if (count + 1 >= utfLength) {
		    try {
			throw new IOException("Invalid UTF-8 encoding found, start of three byte char found at end.");
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}

		byte b = buffer[count++];
		byte c = buffer[count++];
		if (((b & 0xC0) != 0x80) || ((c & 0xC0) != 0x80)) {
		    try {
			throw new IOException("Invalid UTF-8 encoding found, byte two does not start with 0x80.");
		    } catch (IOException e) {
			e.printStackTrace();
		    }
		}

		result[index++] = (char) (((a & 0x0F) << 12) | ((b & 0x3F) << 6) | (c & 0x3F));

	    } else {
		try {
		    throw new IOException("Invalid UTF-8 encoding found, aborting.");
		} catch (IOException e) {
		    e.printStackTrace();
		}
	    }
	}
	return new String(result, 0, index);
    }

    public static JSONArray joinJSONArray(JSONArray mData, JSONArray array) {
	StringBuffer buffer = new StringBuffer();
	try {
	    int len = mData.length();
	    for (int i = 0; i < len; i++) {
		JSONObject obj1 = (JSONObject) mData.get(i);
		if (i == len - 1)
		    buffer.append(obj1.toString());
		else
		    buffer.append(obj1.toString()).append(",");
	    }
	    len = array.length();
	    if (len > 0)
		buffer.append(",");
	    for (int i = 0; i < len; i++) {
		JSONObject obj1 = (JSONObject) array.get(i);
		if (i == len - 1)
		    buffer.append(obj1.toString());
		else
		    buffer.append(obj1.toString()).append(",");
	    }
	    buffer.insert(0, "[").append("]");
	    return new JSONArray(buffer.toString());
	} catch (Exception e) {
	}
	return null;
    }

    public static String get_one_match(String content, String regex) {
	String matched = "";
	Pattern p = Pattern.compile(regex);
	Matcher m = p.matcher(content);
	while (m.find()) {
	    matched = m.group();
	    if (!matched.isEmpty())
		break;
	}
	return matched;
    }

    public static Vector<String> get_all_match(String content, Pattern p) {
	Vector<String> all = new Vector<String>();
	String matched = "";
	Matcher m = p.matcher(content);
	while (m.find()) {
	    matched = m.group();
	    all.add(matched);
	}
	return all;
    }

    public static Vector<String> get_all_match(String content, String regex) {
	Vector<String> all = new Vector<String>();
	String matched = "";
	Pattern p = Pattern.compile(regex);
	Matcher m = p.matcher(content);
	while (m.find()) {
	    matched = m.group();
	    all.add(matched);
	}
	return all;
    }

    public JSONArray get_jsondata_from_js(String valueName, String source) {
	String json = get_one_match(source, valueName + " ='\\[.*?\\]';");
	String res = json.substring(valueName.length() + 3, json.length() - 2);
	JSONArray arr = null;
	try {
	    arr = new JSONArray(res);
	} catch (JSONException e) {
	    e.printStackTrace();
	}
	return arr;
    }

    /**
     * 
        * @Title: gettime  
        * @Description: 获取Unix时间戳 
        * @author qdcz
        * @throws
     */
    public static long gettime() {
	Date d1 = null;
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	Date now = new Date();
	String nowtime = df.format(now);
	long time = 1463812845;
	try {
	    d1 = df.parse(nowtime);
	    time = d1.getTime();
	} catch (ParseException e) {
	    e.printStackTrace();
	}
	return time;
    }

    public static void main(String[] args) {
	int i = 1;
	String str = Function.readFileOneTime("F:\\XPathTest.txt");
	Vector<String> urls = new Vector<String>();
	getMultiResultsByOneXpathPattern(str.getBytes(), "utf-8", "//ul[@class='txtbox clearfix']//li/a/@href", urls);
	for (String url : urls) {
	    System.out.println(i++);
	    System.out.println(url);
	}
    }

    // 一个xpathparser返回一堆结果
    public static void getMultiResultsByOneXpathPattern(byte[] usedata, String charset, String xpathpatter,
	    Vector<String> results) {
	Object[] ns;
	try {
	    String content = new String(usedata, charset);
	    if (xpathpatter.startsWith("$")) {
		List<Object> cnt = JsonPath.read(content.substring(content.indexOf('{')), xpathpatter);
		for (int i = 0; i < cnt.size(); i++) {
		    results.add(cnt.get(i).toString().trim());
		}
	    } else {
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode node = cleaner.clean(content);
		ns = node.evaluateXPath(xpathpatter);

		for (int i = 0; i < ns.length; i++) {
		    String n = ns[i].toString();
		    results.add(n);
		}
		ns = null;
		node = null;
		cleaner = null;
	    }
	    content = null;
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }
}
