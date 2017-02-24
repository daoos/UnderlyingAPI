package com.qdcz.spider.utils;


import com.hpre.coordination.ServiceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class SpiderProperties {
	private static Properties props;
	
	private static final String DEFAULT_EXTERNAL_DIR = "/conf/";
	private static final String DEFAULT_PROPERTIES_FILENAME = "/spider.properties";
	
	static {
		init();
	}
	
	private static void init() {
		// 加载配置文件
		props = new Properties();
		try {
			props.load(new FileInputStream(
					new File(DEFAULT_EXTERNAL_DIR + DEFAULT_PROPERTIES_FILENAME)));
		} catch (IOException e) {
			System.err.println("未加载到:" + new File(DEFAULT_EXTERNAL_DIR + DEFAULT_PROPERTIES_FILENAME));
			System.out.println("尝试加载:" + ServiceManager.class.getResource(DEFAULT_PROPERTIES_FILENAME));
			
			try {
				InputStream inputStream = 
						ServiceManager.class.getResourceAsStream(DEFAULT_PROPERTIES_FILENAME);
				props.load(inputStream);
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(-1);
			}
		}
	}
	
	public static String getProperty(String key) {
		return props.getProperty(key);
	}
	
	public static String getProperty(String key, String defaultValue) {
		return props.getProperty(key, defaultValue);
	}
}
