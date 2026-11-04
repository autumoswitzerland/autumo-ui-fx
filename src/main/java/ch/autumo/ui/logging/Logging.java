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

import java.io.IOException;

/**
 * Logging interface.
 */
public interface Logging {

    /**
     * Initialize logging and return logging file path.
     * <br>
     * - Path used: 'cfg/logging.xml'<br>
     * - Configuration name used: 'default'
     * 
     * @return logging file path
     * @throws IOException when reading the configuration fails
     */
	public String initialize() throws IOException;
	
	/**
	 * Configure the framework specifically with a log configuration file
	 * and name!
	 * 
	 * @param path a path to the configuration
	 * @param name name to the configuration
	 * @throws IOException when reading the configuration fails
	 */
	public void initialize(String path, String name) throws IOException;
	
	/**
	 * Initialize logging with an in-memory-built configuration and appends output to file.
	 * Note: Might have an implementation or not.
	 * 
	 * @param inMemoryConfigName virtual configuration name
	 * @param appName application name that is is used as part of the logging output folder
	 */
	public void initializeInMemoryFile(String inMemoryConfigName, String appName);
	
	/**
	 * Initialize logging with an in-memory-built configuration
	 * Note: Might have an implementation or not.
	 * 
	 * @param inMemoryConfigName virtual configuration name
	 * @param appName application name that is is used as part of the logging output folder
	 */
	public void initializeInMemoryConsole(String inMemoryConfigName, String appName);
	
}
