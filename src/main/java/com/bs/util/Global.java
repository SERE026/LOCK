package com.bs.util;

import com.bs.util.PropertiesLoader;

/**
 * 全局配置类
 * @author wangjw
 * @version 2015-03-11
 */
public class Global {

	/**
	 * 属性文件加载对象
	 * PropertiesLoader
	 */
	private static PropertiesLoader propertiesLoader;
	
	private static String[] resourcePath = new String[]{"appconfig.properties"};
	
	/**
	 * 获取配置
	 */
	public static String getConfig(String key) {
		if (propertiesLoader == null){
			propertiesLoader = new PropertiesLoader(resourcePath);
		}
		return propertiesLoader.getProperty(key);
	}
	
	
	/*public static void main(String[] args) {
		String key = "id";
		String id = getConfig(key);
		System.out.println(id);
	}*/

}
