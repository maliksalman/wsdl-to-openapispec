package com.smalik.wsdl2yamlspec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class YamlGenerator {

	private static Logger logger = LoggerFactory.getLogger(YamlGenerator.class);

	private ClassLoader classLoader;

	public YamlGenerator(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}

	public String generateYaml(NameClassPair ...pairs) throws Exception {
		
		// collect object graph information
		Map<String, ObjectInfo> collector = new LinkedHashMap<>();
		for (NameClassPair pair: pairs) {
			Class pairClass = classLoader.loadClass(pair.getClassName());
			collector.put(pair.getName(), newObjectInfo(pairClass, collector));
		}

		// setup snake yaml dumper
		Representer representer = new Representer() {
			@Override
			protected NodeTuple representJavaBeanProperty(Object javaBean, Property property, Object propertyValue, Tag customTag) {
				if (propertyValue == null) {
					return null;
				} else {
					return super.representJavaBeanProperty(javaBean, property, propertyValue, customTag);
				}
			}
		};
		representer.addClassTag(ObjectInfo.class, Tag.MAP);
		
		// dump the object graph using snake yaml
		Map<String, Map<String, ObjectInfo>> definitions = new HashMap<>();
		definitions.put("definitions", collector);
		return new Yaml(representer).dumpAsMap(definitions);
	}
	
	private ObjectInfo newObjectInfo(Class type, Map<String, ObjectInfo> collector) throws Exception {

		logger.debug("Adding ObjectInfo, Class=" + type.getName());

		ObjectInfo info = new ObjectInfo();
		info.setType("object");
		
		BeanInfo beanInfo = Introspector.getBeanInfo(type);
		for (PropertyDescriptor descriptor : beanInfo.getPropertyDescriptors()) {
			String name = descriptor.getName();
			if ("class".equals(name)) {
				continue;
			}
			
			Class propertyType = descriptor.getPropertyType();
			switch(propertyType.getName()) {			

				case "java.lang.String": 
					addSimpleProperty(info, name, "string");
					break;

				case "boolean":
				case "java.lang.Boolean": 
					addSimpleProperty(info, name, "boolean");
					break;

				case "double":
				case "java.lang.Double":
				case "java.math.BigDecimal":
					addSimpleProperty(info, name, "number", "double");
					break;

				case "int":
				case "java.lang.Integer":
					addSimpleProperty(info, name, "integer", "int32");
					break;

				case "long":
				case "java.lang.Long": 
					addSimpleProperty(info, name, "integer", "int64");
					break;

				case "javax.xml.datatype.XMLGregorianCalendar":
				case "java.util.Date":
					addSimpleProperty(info, name, "string", "date-time");
					break;
					
				case "java.util.List":
					addListProperty(info, descriptor, name, type, collector);
					break;

				default:
					addObjectReferenceProperty(info, name, propertyType, type, collector);
			}
		}
		
		return info;
	}


	private void addListProperty(ObjectInfo info, PropertyDescriptor descriptor, String name, Class beanType,
								 Map<String, ObjectInfo> collector) throws ClassNotFoundException, Exception {
		
		Type genericReturnType = descriptor.getReadMethod().getGenericReturnType();
		if (genericReturnType instanceof ParameterizedType) {
			ParameterizedType pt = (ParameterizedType) genericReturnType;
			Class parameterizedCollectionType = classLoader.loadClass(pt.getActualTypeArguments()[0].getTypeName());

			// TODO: assuming that the parameterized type is of complex type - need to handle parameterized type of simple objects
			if (beanType != parameterizedCollectionType && !collector.containsKey(parameterizedCollectionType.getSimpleName())) {
				collector.put(parameterizedCollectionType.getSimpleName(), newObjectInfo(parameterizedCollectionType, collector));
			}
			
			ObjectProperty prop = new ObjectProperty();
			prop.setType("array");
			prop.setDescription("a list of " + parameterizedCollectionType.getSimpleName());
			prop.setItems(new ObjectReference().with$ref("#/definitions/" + parameterizedCollectionType.getSimpleName()));
			info.getProperties().put(name, prop);
		}
	}


	private void addObjectReferenceProperty(ObjectInfo info, String name, Class propertyType, Class beanType, Map<String, ObjectInfo> collector)
			throws Exception {
		if (beanType != propertyType && !collector.containsKey(propertyType.getSimpleName())) {
			collector.put(propertyType.getSimpleName(), newObjectInfo(propertyType, collector));
		}

		ObjectProperty prop = new ObjectProperty();
		prop.set$ref("#/definitions/" + propertyType.getSimpleName());
		info.getProperties().put(name, prop);
	}


	private void addSimpleProperty(ObjectInfo info, String name, String simpleType) {
		ObjectProperty prop = new ObjectProperty();
		prop.setType(simpleType);
		prop.setDescription("the " + name + " " + simpleType);
		info.getProperties().put(name, prop);
	}
	
	private void addSimpleProperty(ObjectInfo info, String name, String simpleType, String format) {
		ObjectProperty prop = new ObjectProperty();
		prop.setType(simpleType);
		prop.setFormat(format);
		prop.setDescription("the " + name + " " + simpleType);
		info.getProperties().put(name, prop);
	}
}
