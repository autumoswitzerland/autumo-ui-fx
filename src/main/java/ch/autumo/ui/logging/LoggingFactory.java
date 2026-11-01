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
package ch.autumo.ui.logging;

import java.lang.reflect.Constructor;

import ch.autumo.ui.utils.ConfigurationManager;

/**
 * Factory for the configured {@link Logging} implementation (defaults to Log4j2).
 */
public class LoggingFactory {

	private static Logging logging;
	
	/**
	 * Private constructor.
	 */
	private LoggingFactory() {
	}
	
	/**
	 * Get logging implementation class.
	 * 
	 * @return logging implementation
	 * @throws Exception if logging instance cannot be acquired
	 */
	public static synchronized Logging getInstance() throws Exception {
		
		if (logging == null) {
			
			String impl = null;
			
			if (ConfigurationManager.isInitialized()) {
				impl = ConfigurationManager.getInstance().getString("logging_implementation");
				if (impl == null || impl.length() == 0) {
					// default: log4j2
					impl = "ch.autumo.ui.logging.Log4j2Logging";
				}
			} else {
				// usually rich client side, we allow this
				impl = "ch.autumo.ui.logging.Log4j2Logging";				
			}
			
	        final Constructor<?> constructor = Class.forName(impl).getDeclaredConstructor();
	        constructor.setAccessible(true);
	        logging = (Logging) constructor.newInstance();
	        // no initialization here, there are several initialization methods
		}
		
		return logging;
	}
	
}
