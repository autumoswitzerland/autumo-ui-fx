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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Central configuration manager: loads, reads and writes application configuration
 * (properties and XML) from the configured root path.
 */
public class ConfigurationManager {

	/**
	 * Logger.
	 */
	protected static final Logger LOG = LoggerFactory.getLogger(ConfigurationManager.class.getName());
	
	/**
	 * Yes!
	 */
	public static final String YES = "yes";
	
	/** Instance */
	private static ConfigurationManager manager = null;
	
	/** Initialized? */
	protected static boolean isInitialized = false;

	/** The general configuration properties ('cfg/app.cfg'). */
	protected Properties generalProps = null;
	
	
	/**
	 * Default constructor.
	 */
	private ConfigurationManager() {
	}
	
	/**
	 * Get configuration manager.
	 * 
	 * @return manager
	 */
	public static synchronized ConfigurationManager getInstance() {
		if (manager == null) {
			manager = new ConfigurationManager();
		}
		return manager;
	}
	
	/**
	 * Is inizialized?
	 * 
	 * @return true if initialized
	 */
	public static boolean isInitialized() {
		return isInitialized;
	}	
	
	/**
	 * Get a string value.
	 * 
	 * @param key key
	 * @return value
	 */
	public String getString(String key) {
		final String v = generalProps.getProperty(key);
		if (v != null) {
			return v.trim();
		} else { 
			LOG.warn("Value for key '{}' doesn't exist in configuration!", key);
			return null;
		}
	}
	
	/**
	 * Get a string value or default value if non-existent.
	 * 
	 * @param key key
	 * @param defaultVal default value
	 * @return value
	 */
	public String getString(String key, String defaultVal) {
		final String v = generalProps.getProperty(key);
		if (v != null) {
			return v.trim();
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Get a string value; no warning if value is not available.
	 * 
	 * @param key key
	 * @return value
	 */
	public String getStringNoWarn(String key) {
		String v = generalProps.getProperty(key);
		if (v != null)
			v = v.trim();
		return v;
	}
	
	/**
	 * Get all keys starting with a specific key-prefix.
	 * 
	 * @param keyPrefix key prefix, e.g. 'dispatcher_'
	 * @return collected values
	 */
	public String[] getKeys(String keyPrefix) {
		final List<String> collectedKeys = new ArrayList<>();
		final Set<Object> keys = generalProps.keySet();
		for (Iterator<Object> iterator = keys.iterator(); iterator.hasNext();) {
			final String key = (String) iterator.next();
			if (key.startsWith(keyPrefix))
				collectedKeys.add(key);
		}
		return collectedKeys.toArray(new String[collectedKeys.size()]);
	}
	
	/**
	 * Get all values starting with a specific key-prefix.
	 * 
	 * @param keyPrefix key prefix, e.g. 'dispatcher_'
	 * @return collected values
	 */
	public String[] getValues(String keyPrefix) {
		final List<String> collectedVals = new ArrayList<>();
		final Set<Object> keys = generalProps.keySet();
		for (Iterator<Object> iterator = keys.iterator(); iterator.hasNext();) {
			final String key = (String) iterator.next();
			if (key.startsWith(keyPrefix))
				collectedVals.add(generalProps.getProperty(key));
		}
		return collectedVals.toArray(new String[collectedVals.size()]);
	}

	
	/**
	 * Get integer value.
	 * 
	 * @param key key
	 * @return integer value
	 */
	public int getInt(String key) {
		String v = generalProps.getProperty(key);
		if (v == null || v.length() == 0) {
			LOG.warn("Value for key '{}' doesn't exist in beetroot configuration!", key);
			return -1;
		}
		return Integer.valueOf(v);
	}

	/**
	 * Get integer value.
	 * 
	 * @param key key
	 * @param defaultVal default value or default value if non-existent.
	 * @return integer value
	 */
	public int getInt(String key, int defaultVal) {
		String v = generalProps.getProperty(key);
		if (v == null || v.length() == 0)
			return defaultVal;
		return Integer.valueOf(v);
	}
	
	/**
	 * Get integer value; no warning if value is not available.
	 * 
	 * @param key key
	 * @return integer value
	 */
	public int getIntNoWarn(String key) {
		String v = generalProps.getProperty(key);
		if (v == null || v.length() == 0) {
			return -1;
		}
		return Integer.valueOf(v);
	}
	
	/**
	 * Get yes (true) or no (false), if the configuration is messed up false
	 * is returned.
	 * 
	 * @param key key
	 * @return true or false
	 */
	public boolean getYesOrNo(String key) {
		String val = generalProps.getProperty(key);
		if (val == null || val.length() == 0) {
			LOG.warn("Value for yes/no key '{}' doesn't exist in beetroot configuration!", key);
			return false;
		}
		val = val.trim();
		return val.equalsIgnoreCase(YES);
	}	

	/**
	 * Get yes (true) or no (false), if the configuration is messed up false
	 * is returned. No warning if key is missing.
	 * 
	 * @param key key
	 * @return true or false
	 */
	public boolean getYesOrNoNoWarn(String key) {
		String val = generalProps.getProperty(key);
		if (val == null || val.length() == 0)
			return false;
		val = val.trim();
		return val.equalsIgnoreCase(YES);
	}
	
	/**
	 * Customized initialization with already loaded properties.
	 * Can be called more than once.
	 * 
	 * @param config configuration properties
	 * @throws Exception if initilization fails
	 */
	public synchronized void initializeWithProperties(Properties config) throws Exception {
		if (isInitialized) {
    		LOG.warn("WARNING: Initialisation of configuration manager is called more than once!");
    		return;
		}

		generalProps = config;
		// At last
		isInitialized = true;
	}	
}
