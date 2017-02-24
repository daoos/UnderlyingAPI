package com.qdcz.spider.utils;

import java.util.HashMap;
import java.util.Map;


public class SearchConfigure {
	
	
	public SearchConfigure(){
		loadAllConfiguration();
	}
	
	private Map<String,String> config = null;
	
	public  void loadAllConfiguration(){
		if(config==null){
			String configurePath = SpiderProperties.getProperty("conf") + "/CompanyInformationRelationConfigure.txt";
			String configContent = Function.readFileOneTime(configurePath);
			String[] confArr = configContent.split("\n");
			config = new HashMap<String,String>();
			for(String line:confArr){
				if(line== null || line.startsWith("#") || line.equals(""))
					continue;
				
				line = line.trim();
				String[] arr = line.split("=");
				if(arr.length==2){
					config.put(arr[0], arr[1]);
				}
			}
		}
	}

	public  String getConfig(String configName){
		if(config!=null){
			return config.get(configName);
		}
		return null;
	}
	
	public  void showAllConfiguration(){
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
