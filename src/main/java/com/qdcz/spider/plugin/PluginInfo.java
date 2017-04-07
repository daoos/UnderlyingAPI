package com.qdcz.spider.plugin;

import java.util.Vector;

/**
 * 插件的实体类
 */
public class PluginInfo {
	public String jarName;
	public String url;
	public Vector<String> helpClassList;//辅助类，不需实例化
	public Vector<String> instanceClassList;//解析类，需实例化
	
	public PluginInfo(String jarName, String url){
		this.jarName = jarName;
		this.url = url;
		helpClassList = new Vector<String>();
		instanceClassList = new Vector<String>();
	}
	
	public void addHelpClass(String classPath){
		this.helpClassList.add(classPath);
	}
	
	public void addInstanceClass(String classPath){
		this.instanceClassList.add(classPath);
	}
}
