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

import ch.autumo.ui.utils.OSUtils;

/**
 * Abstract base class for {@link Logging} implementations, providing the
 * application root path resolution.
 */
public abstract class AbstractLogging implements Logging {

	/**
	 * Default constructor.
	 */
	protected AbstractLogging() {
	}
	
	/** ROOT path. */
    protected static String rootPath = null;
    static {
    	rootPath = System.getProperty("ROOTPATH");
    	if (rootPath == null || rootPath.length() == 0)
    		rootPath = "." + OSUtils.FILE_SEPARATOR;
    	if (!rootPath.endsWith(OSUtils.FILE_SEPARATOR))
    		rootPath += OSUtils.FILE_SEPARATOR;
    }
    
}
