package com.adalrsjr.util

import groovy.transform.CompileStatic;

@CompileStatic
class Configuration {
	private final static String PATH = "src/main/resources/app.properties"
	private static Configuration INSTANCE
	
	Properties properties
	
	private Configuration(String path) {
		InputStream is = new FileInputStream(path)
		properties = new Properties()
		properties.load(is)
	}
	
	static Configuration getInstance() {
		if(!INSTANCE)
			INSTANCE = new Configuration(PATH)
		return INSTANCE
	}
	
	
	public static void main(String[] args) {
		Configuration c = new Configuration("src/main/resources/app.properties")
		println c.properties
	}
	
}
