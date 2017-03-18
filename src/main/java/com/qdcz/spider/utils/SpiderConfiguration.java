package com.qdcz.spider.utils;

import java.util.HashMap;
import java.util.Map;


public class SpiderConfiguration {
	private static Map<String,String> config = null;
	
	static{
		loadAllConfiguration();
	}
	
	public static void loadAllConfiguration(){
		if(config==null){
			String configurePath="conf\\spider_new.conf";
			String configContent =Function.readFileOneTime(configurePath);
//			String configurePath="/usr/hadoop/spiderV3/conf/spider.conf";
//			String configContent = HDFSOperations.readConfigFile(configurePath);
			String[] confArr = configContent.split("\n");
			config = new HashMap<String,String>();
			for(String line:confArr){
				if(line== null || line.startsWith("#") || line.equals(""))
					continue;
				String[] arr = line.split("=");
				if(arr.length==2){
					config.put(arr[0].trim(), arr[1].trim());
				}
			}
		}
	}
	
	public static String getConfig(String configName){
		if(config!=null){
			return config.get(configName);
		}
		return null;
	}
	
	public static Integer getConfigInt(String configName){
		if(config!=null){
			try{
				Integer value = Integer.parseInt(config.get(configName));
				return value;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Long getConfigLong(String configName){
		if(config!=null){
			try{
				Long value = Long.parseLong(config.get(configName));
				return value;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Boolean getConfigBoolean(String configName){
		if(config!=null){
			try{
				Boolean value = Boolean.parseBoolean(config.get(configName));
				return value;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static void showAllConfiguration(){
		if(config==null){
			loadAllConfiguration();
		}
		if(config!=null){
			for(String key:config.keySet()){
				System.out.println(key+":"+config.get(key));
			}
		}
	}
}
