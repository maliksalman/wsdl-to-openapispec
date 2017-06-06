package com.smalik.wsdl2yamlspec;

public class ObjectProperty {

	private ObjectReference items;
	private String $ref;
	private String format;
	private String type;
	private String description;

	public void setType(String type) {
		this.type = type;
	}

	public String get$ref() {
		return $ref;
	}

	public void set$ref(String $ref) {
		this.$ref = $ref;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public ObjectReference getItems() {
		return items;
	}

	public void setItems(ObjectReference items) {
		this.items = items;
	}
}
