package com.qdcz.spider.plugin;

import java.util.HashMap;
import java.util.Map;

import com.qdcz.spider.utils.Function;
import com.qdcz.spider.utils.SpiderProperties;

public class Configuration {
	private static Map<String,String> config = null;
	
	static{
		loadAllConfiguration();
	}
	public static void loadAllConfiguration(){
		if(config==null){
			String configurePath = SpiderProperties.getProperty("conf") + "/spider.conf";
			String configContent = Function.readFileOneTime(configurePath);
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
	
	public static String getConf(String dir,String fileName){
		String configurePath = SpiderProperties.getProperty(dir) + Configuration.getConfig(fileName);
		String configContent = Function.readFileOneTime(configurePath);
		return configContent;
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
				String port = config.get(configName);
				System.out.println(port);
				Integer value = Integer.parseInt(port);
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
