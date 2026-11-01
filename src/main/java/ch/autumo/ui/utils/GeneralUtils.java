/**
 * Copyright (c) 2026 autumo Ltd. Switzerland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ch.autumo.ui.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * General-purpose utility methods (string, file, path, date, ...).
 */
public class GeneralUtils {

	/**
	 * Default constructor.
	 */
	private GeneralUtils() {
	}
	
	/**
	 * Get an exception stack trace as text.
	 * 
	 * @param throwable thropwable/exception
	 * @return stack trace text
	 */
	public static String getStackTrace(final Throwable throwable) {
	     final StringWriter sw = new StringWriter();
	     final PrintWriter pw = new PrintWriter(sw, true);
	     throwable.printStackTrace(pw);
	     return sw.getBuffer().toString();
	}

	/**
	 * Get an exception stack trace as html.
	 * 
	 * @param throwable thropwable/exception
	 * @return stack trace html
	 */
	public static String getHtmlStackTrace(final Throwable throwable) {
		
		return getStackTrace(throwable).replaceAll(OSUtils.LINE_SEPARATOR, "<br>\n");
	}
	
	/**
	 * Copy props to destination props. Existing destination properties are not overwritten !
	 * 
	 * @param destination destination props
	 * @param toCopy props to copy into destination
	 * @return destination props with values of the props copied 
	 */
	public static Properties copyProperties(Properties destination, Properties toCopy) {
		return copyProperties(destination, toCopy, false);
	}

	/**
	 * Copy props to destination props. 
	 * 
	 * @param destination destination props
	 * @param toCopy props to copy into destination
	 * @param overwrite true, if existing destination properties should be overwritten
	 * @return destination props with values of the props copied 
	 */
	public static Properties copyProperties(Properties destination, Properties toCopy, boolean overwrite) {
        final Set<Object> propertyNames = toCopy.keySet();
        for (Object key : propertyNames) {
        	final String name = key.toString();
            final String propertyValue = toCopy.getProperty(name);
        	if (overwrite) {
        		
                if (destination.getProperty(name) != null)
                	destination.replace(name, propertyValue);
                else
                	destination.setProperty(name, propertyValue);
        	} else {
        		
                if (destination.getProperty(name) == null)
                	destination.setProperty(name, propertyValue);
        	}
        }
	    return destination;
	}
	
	/**
	 * Print properties.
	 * 
	 * @param props props
	 */
	public static void printProperties(Properties props) {
        final Set<String> propertyNames = props.stringPropertyNames();
        for (String name : propertyNames) {
            String propertyValue = props.getProperty(name);
            System.out.println("PROP: "+name+"="+propertyValue);
        }
	}
	
	/**
	 * Create map.
	 * 
	 * @param keys keys
	 * @param values values
	 * @return map
	 */
	public static Map<String, String> createMap(String keys[], String values[]) {
		if (keys.length != values.length)
			throw new RuntimeException("GeneralUtil.createMap: not same amount of keys and values!");
		final Map<String, String> map = new HashMap<String, String>();
		for (int i = 0; i < keys.length; i++) {
			map.put(keys[i], values[i]);
		}
		return map;
	}
	
}
