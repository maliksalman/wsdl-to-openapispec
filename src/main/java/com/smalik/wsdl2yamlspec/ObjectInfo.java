package com.smalik.wsdl2yamlspec;

import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectInfo {
	
	private String type;
	private Map<String, ObjectProperty> properties = new LinkedHashMap<>() ;
	
	public void setType(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
	
	public void setProperties(Map<String, ObjectProperty> properties) {
		this.properties = properties;
	}

	public Map<String, ObjectProperty> getProperties() {
		return properties;
	}
}

