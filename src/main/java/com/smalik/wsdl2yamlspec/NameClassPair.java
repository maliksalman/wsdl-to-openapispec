package com.smalik.wsdl2yamlspec;

public class NameClassPair {

    private String name;
	private String className;

	public NameClassPair(String name, String className) {
		this.name = name;
		this.className = className;
	}

	public String getName() {
		return name;
	}

	public String getClassName() {
		return className;
	}
}
