package com.smalik.wsdl2yamlspec;

public class ObjectReference {
	private String $ref;

	public String get$ref() {
		return $ref;
	}

	public void set$ref(String $ref) {
		this.$ref = $ref;
	}

	public ObjectReference with$ref(String $ref) {
		this.$ref = $ref;
		return this;
	}
}