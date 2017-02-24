package com.qdcz.spider.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qdcz.spider.http.SearchResult;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


public class JSONUtils {
	
	public static void jsonFormatDump(String path, Object obj) throws IOException {
		jsonFormatDump(path, obj, true);
	}
	
	public static void jsonFormatDump(String path, Object obj, boolean append) 
			throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(path);
		BufferedOutputStream outStream = new BufferedOutputStream(
				new FileOutputStream(file, append));
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		
		mapper.writeValue(bytes, obj);
		outStream.write(bytes.toByteArray());
		outStream.write("\n".getBytes());
		outStream.close();
	}
	
	public static <T> List<T> jsonFormatRestore(String path, Class<? extends T> clas) 
			throws FileNotFoundException {
		List<T> list = new ArrayList<T>();
		ObjectMapper mapper = new ObjectMapper();
		File file = new File(path);
		Scanner scanner = new Scanner(new FileReader(file));
		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			
			try {
				list.add(mapper.readValue(line.getBytes(), clas));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		scanner.close();
		return list;
	}
	
	public static String jsonIndent(String jsonString, int indentFactor) throws JSONException {
		JSONObject json = new JSONObject(jsonString);
		return json.toString(indentFactor);
	}
	
	
	/**
	 * 将传进来的对象转换成对应的字节
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public static String jsonFormatStr(List<SearchResult> total)
			throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		ByteArrayOutputStream bytes = null;
		StringBuffer sb = new StringBuffer();
		for (SearchResult searchResult : total) {
			bytes = new ByteArrayOutputStream();
			mapper.writeValue(bytes, searchResult);
			sb.append(new String(bytes.toByteArray())).append("\n");
		}
		return sb.toString();
	}
	/**
	 * 用于将传进来的字符转转换成对应的list
	 * @param str
	 * @param clas
	 * @return
	 * @throws FileNotFoundException
	 */
	public static <T> List<T> jsonFormatStr(String str, Class<? extends T> clas) 
			throws FileNotFoundException {
		List<T> list = new ArrayList<T>();
		ObjectMapper mapper = new ObjectMapper();
		Scanner scanner = new Scanner(new BufferedInputStream(new ByteArrayInputStream(str.getBytes())));
		
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			
			try {
				list.add(mapper.readValue(line.getBytes(), clas));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		scanner.close();
		return list;
	}
}
