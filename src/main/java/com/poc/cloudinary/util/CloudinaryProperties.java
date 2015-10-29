package com.poc.cloudinary.util;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public enum CloudinaryProperties {

	CREDENTIALS;
	
	private static final String path = "cloudinary.properties";
	private static Properties properties;
	private String value;
	private static final Map<String, String> api_credentials = new HashMap<>();
	
	private void init() {
		if(properties == null) {
			properties = new Properties();
		}
		try {
			properties.load(this.getClass().getClassLoader().getResourceAsStream(path));
		} catch (IOException ioEx) {
			throw new RuntimeException("Unable to load cloudinary properties file", ioEx);
		}
		
		api_credentials.put("CLOUD_NAME", (String)properties.getProperty("CLOUD_NAME"));
		api_credentials.put("API_KEY", (String)properties.getProperty("API_KEY"));
		api_credentials.put("API_SECRET", (String)properties.getProperty("API_SECRET"));
	}
	
	public Map<String, String> getApiCredentials() {
		if(api_credentials.isEmpty()) {
			init();
		}
		return api_credentials;
	}
}
