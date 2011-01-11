/**
 *  Copyright 2010 Wallace Wadge
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.jolbox.bonecp;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;

import org.junit.Test;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.Annotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;

/**
 * Not really a test - this creates the bonecp-default-config.xml by reading BonecpConfig.java. 
 * 
 * @author wallacew
 * 
 */
public class TestXMLDefaultCreate {
	

	/**
	 * @throws IllegalAccessException
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws IOException 
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 */
	@Test
	public void testCreateXMLFile() throws SecurityException, IllegalArgumentException, IllegalAccessException, IOException, InvocationTargetException, NoSuchMethodException {
		BoneCPConfig config = new BoneCPConfig();
		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<!-- Default options loaded by BoneCP. Modify as per your needs. This file has\n");
		sb.append("     been automatically generated. -->\n");
		sb.append("<bonecp-config>\n");
		sb.append("\t<default-config>\n");

		JavaDocBuilder builder = new JavaDocBuilder();

		// find the file (cater for maven/eclipse workflows)
		File f = new File("bonecp/src/main/java/com/jolbox/bonecp/BoneCPConfig.java");
		File out;
		FileReader fr;
		if (f.canRead()){
			fr = new FileReader(f);
			out = new File("bonecp/src/main/resources/bonecp-default-config.xml");
		} else {
			fr = new FileReader("src/main/java/com/jolbox/bonecp/BoneCPConfig.java");
			out = new File("src/main/resources/bonecp-default-config.xml");
		}
		builder.addSource(fr);
		JavaClass cls = builder
				.getClassByName("com.jolbox.bonecp.BoneCPConfig");
		for (JavaMethod method : cls.getMethods()) {
			String mName = method.getName();
			if (mName.startsWith("set") && method.isPublic()) {
				Annotation[] a = method.getAnnotations();
				if (method.getParameters().length == 1 
						&& !method.getParameters()[0].getType().getJavaClass().getFullyQualifiedName().equals("java.util.Properties") 
						&&  !(a.length > 0 && a[0].getType().getValue()
						.equals("java.lang.Deprecated"))) {

					sb.append(formatComment(method.getComment()));
					String prop = toProperty(mName);
					Object defaultVal;
					try {
						
						defaultVal = fetchDefaultValue(config, prop);

						if (defaultVal == null) {
							sb.append("\t\t<!-- <property name=\""
									+ prop
									+ "\">(null or no default value)</property> -->\n\n");
						} else {
							sb.append("\t\t<property name=\"" + prop + "\">"
									+ defaultVal + "</property>\n\n");
						}
					} catch (NoSuchFieldException e) {
						// ignore it
					}
				}
			}
		}

		sb.append("\t</default-config>\n");
		sb.append("</bonecp-config>\n");
		FileWriter fw = new FileWriter(out);
		fw.write(sb.toString());
		fw.close();
	}

	/**
	 * @param config
	 * @param prop
	 * @return default value
	 * @throws SecurityException
	 * @throws NoSuchFieldException
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws NoSuchMethodException 
	 * @throws InvocationTargetException 
	 */
	private static Object fetchDefaultValue(BoneCPConfig config, String prop)
			throws SecurityException,
			IllegalArgumentException, IllegalAccessException, NoSuchFieldException, InvocationTargetException, NoSuchMethodException {
		Field field = null ;
	
		try{
			field = BoneCPConfig.class.getDeclaredField(prop);
		} catch (NoSuchFieldException e){
		
			if (field == null){
				return BoneCPConfig.class.getDeclaredMethod("get"+prop.substring(0, 1).toUpperCase()+prop.substring(1)).invoke(config);
			}
		}
		
		field.setAccessible(true);
		return field.get(config);
	}

	/**
	 * @param comment
	 * @return the comment, formatted nicely.
	 */
	private static String formatComment(String comment) {
		StringBuilder sb = new StringBuilder();
		String tmp = comment.replaceAll("<p>", "").replaceAll("\n", " ");
		sb.append("\t\t<!-- ");
		int i = 0;
		for (String s : tmp.split(" ")) {
			i++;
			sb.append(s + " ");
			if (i % 13 == 0) {
				sb.append("\n\t\t     ");
			}
		}

		return sb.toString() + "-->\n";
	}

	/**
	 * @param name
	 * @return property
	 */
	private static String toProperty(String name) {
		return String.valueOf(name.charAt(3)).toLowerCase() + name.substring(4);
	}

}
