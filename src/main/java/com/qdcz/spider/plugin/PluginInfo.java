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

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		PluginInfo that = (PluginInfo) o;

		if (jarName != null ? !jarName.equals(that.jarName) : that.jarName != null) return false;
		if (url != null ? !url.equals(that.url) : that.url != null) return false;
		if (helpClassList != null ? !helpClassList.equals(that.helpClassList) : that.helpClassList != null)
			return false;
		return instanceClassList != null ? instanceClassList.equals(that.instanceClassList) : that.instanceClassList == null;

	}

	@Override
	public int hashCode() {
		int result = jarName != null ? jarName.hashCode() : 0;
		result = 31 * result + (url != null ? url.hashCode() : 0);
		result = 31 * result + (helpClassList != null ? helpClassList.hashCode() : 0);
		result = 31 * result + (instanceClassList != null ? instanceClassList.hashCode() : 0);
		return result;
	}
}
