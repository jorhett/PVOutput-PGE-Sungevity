package com.droidbytes.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PVProperties {

	private static Properties configProp = null;

	private static void loadProps() {
		ClassLoader classloader = Thread.currentThread()
				.getContextClassLoader();
		InputStream in = classloader.getResourceAsStream("config.properties");
		try {
			configProp = new Properties();
			configProp.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String getProperty(String propName) {
		if (configProp == null) {
			loadProps();
		}
		return configProp.getProperty(propName);
	}
}
