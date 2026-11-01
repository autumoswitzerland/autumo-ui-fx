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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;

import ch.autumo.ui.Constants;
import ch.autumo.ui.utils.ConfigurationManager;
import ch.autumo.ui.utils.OSUtils;

/**
 * Log4j2 Logging initialization.
 */
public class Log4j2Logging extends AbstractLogging {

	/** Log file */
	public static String SYSTEM_PROPERTY_LOG_FILE = "log4j2.configurationFile";
	
	private boolean initialized = false;
	private String logCfgFile = null;
	
	/**
	 * Default constructor.
	 */
	public Log4j2Logging() {
		super();
	}
	
    @Override
	public String initialize() throws IOException {
		if (initialized)
			return logCfgFile;
		logCfgFile = System.getProperty(SYSTEM_PROPERTY_LOG_FILE);
		// check if it has been overwritten with the 'log4j.configuration' parameter
		if (logCfgFile != null) {
			// --> -Dlog4j.configuration=file:<log-cfg-path>/logging.cfg
			// --> In this case nothing to do, log4j reads the path!
		} else {
			logCfgFile = "cfg/logging.xml";
			this.initialize(rootPath + logCfgFile, "default");
		}
		
		initialized = true;
		return logCfgFile;
	}

    @Override
	public void initialize(String path, String name) throws IOException {
    	// if configuration is given by the command line parameter don't read the given configuration file 
    	if (System.getProperty(SYSTEM_PROPERTY_LOG_FILE) != null)
    		return;
    	
		final File cf = new File(path);
		if (cf.exists()) {
			Configurator.initialize(name, null, cf.toURI());
		}
		else {
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
			ConfigurationSource cs = new ConfigurationSource(is);
			Configurator.initialize(Thread.currentThread().getContextClassLoader(), cs);
		}
	}
	
    @Override
	public void initializeInMemoryFile(String inMemoryConfigName, String appName) {
    	if (initialized)
			return;
		this.initializeInMemory(inMemoryConfigName, appName, Level.ERROR, "file", "ch.autumo");
		initialized = true;
	}

    @Override
	public void initializeInMemoryConsole(String inMemoryConfigName, String appName) {
    	if (initialized)
			return;
		this.initializeInMemory(inMemoryConfigName, appName, Level.ERROR, "console", "ch.autumo");
		initialized = true;
	}
	
	/**
	 * Initialize logging with an in-memory-built log4j2 configuration
	 * 
	 * @param inMemoryConfigName virtual configuration name
	 * @param appName application name that is is used as part 
	 * 			of the logging output folder and log file name
	 * @param level log level
	 * @param appenderRef file|console
	 * @param packageName package name
	 */
	public void initializeInMemory(String inMemoryConfigName, String appName, Level level, String appenderRef, String packageName) {

        final File logPath = new File(OSUtils.getPropertiesPath(appName) + "logs");
        if (!logPath.exists())
            logPath.mkdirs();

    	
    	final ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();
    	builder.setStatusLevel(level);
    	builder.setConfigurationName(inMemoryConfigName);
    	
    	
    	// Log Pattern configured? 
    	String filePattern    = "%-5p %d{yyyyMMdd-HH:mm:ss.SSS} [%-26.26t] %-35.35c{1.1.1.*} : %.-1000m%ex%n";
    	String consolePattern = "%highlight{%-5p}{TRACE=white} %style{%d{yyyyMMdd-HH:mm:ss.SSS}}{bright_black} %style{[%-26.26t]}{magenta} %style{%-35.35c{1.1.1.*}}{cyan} %style{:}{bright_black} %.-1000m%ex%n";
    	if (ConfigurationManager.isInitialized()) {
    		filePattern = ConfigurationManager.getInstance().getString(Constants.PROP_KEY_LOG_FILE_PATTERN, filePattern);
    		consolePattern = ConfigurationManager.getInstance().getString(Constants.PROP_KEY_LOG_CONSOLE_PATTERN, consolePattern);
    		final String showAnsiColors = ConfigurationManager.getInstance().getString("console_colors", "yes");
    		if (showAnsiColors.equalsIgnoreCase("no"))
    			consolePattern = filePattern;
    	}
    	
    	
    	AppenderComponentBuilder appenderBuilder = null;
    	LayoutComponentBuilder layoutBuilder = null;
    	
    	
    	// create a console appender
    	layoutBuilder = builder.newLayout("PatternLayout")
    			.addAttribute("pattern", consolePattern);
    	appenderBuilder = builder.newAppender("console", "Console")
	    		.addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
	    		.add(layoutBuilder);
    	builder.add(appenderBuilder);
    	
    	
    	// create a rolling file appender
    	layoutBuilder = builder.newLayout("PatternLayout")
    			.addAttribute("pattern", filePattern)
    			.addAttribute("disableAnsi", "false");
    	final ComponentBuilder<?> triggeringPolicy = builder.newComponent("Policies")
	    	    .addComponent(builder.newComponent("TimeBasedTriggeringPolicy").addAttribute("interval", "1").addAttribute("modulate", "true"))
	    	    .addComponent(builder.newComponent("SizeBasedTriggeringPolicy").addAttribute("size", "10M")
	    	);
    	final ComponentBuilder<?> rolloverStrategy = builder.newComponent("DefaultRolloverStrategy").addAttribute("max", "10")
	    		.addComponent(builder.newComponent("Delete").addAttribute("basePath", logPath).addAttribute("maxDepth", "10")
	    		.addComponent(builder.newComponent("IfLastModified").addAttribute("age", "30d")
    		)
    	);
    	final char c[] = appName.toCharArray();
    	c[0] = Character.toLowerCase(c[0]);
    	appName = new String(c);
    	appenderBuilder = builder.newAppender("file", "RollingFile")
	    	    .addAttribute("fileName", logPath + "/"+appName+".log")
	    	    .addAttribute("filePattern", logPath + "/"+appName+"-%d{yyyyMMdd}.log")
	    	    .add(layoutBuilder)
	    	    .addComponent(triggeringPolicy)
	    	    .addComponent(rolloverStrategy);
    	builder.add(appenderBuilder);
    	
    	
    	// create the new logger
    	builder.add(builder.newLogger(packageName, level)
    	    .add(builder.newAppenderRef(appenderRef))
    	    .addAttribute("additivity", false));

    	builder.add(builder.newRootLogger(level)
    		.add(builder.newAppenderRef(appenderRef))
    		.addAttribute("additivity", false));

    	
    	//System.err.println(builder.toXmlConfiguration());
    	
    	
    	Configurator.reconfigure(builder.build());
    	
		initialized = true;
    }
	
}
