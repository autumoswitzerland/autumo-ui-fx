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
package ch.autumo.ui.fx;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.autumo.ui.Constants;
import ch.autumo.ui.exception.ProcessException;
import ch.autumo.ui.fx.controller.BaseController;
import ch.autumo.ui.fx.controller.actions.installer.InstallAction;
import ch.autumo.ui.fx.controller.actions.installer.InstallEndListener;
import ch.autumo.ui.fx.view.BaseAnchorView;
import ch.autumo.ui.fx.view.BaseBorderView;
import ch.autumo.ui.fx.view.BaseView;
import ch.autumo.ui.fx.view.DefaultCodeEditorView;
import ch.autumo.ui.fx.view.SplashScreen;
import ch.autumo.ui.license.LicensedApplication;
import ch.autumo.ui.logging.LoggingFactory;
import ch.autumo.ui.utils.CommandLine;
import ch.autumo.ui.utils.ConfigurationManager;
import ch.autumo.ui.utils.CssLiveReload;
import ch.autumo.ui.utils.FontRegistry;
import ch.autumo.ui.utils.GeneralUtils;
import ch.autumo.ui.utils.Helper;
import ch.autumo.ui.utils.Messages;
import ch.autumo.ui.utils.OSUtils;
import ch.autumo.ui.utils.UI;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;


/**
 * The Java FX application.
 *
 * In any case you always can use the getters from
 * {@link ch.autumo.ui.utils.ConfigurationManager}
 * to retrieve any configuration parameter.
 *
 * <p><b>Two hooks an application usually overrides:</b></p>
 * <ul>
 *   <li>{@link #isSplashScreenEnabled()} and {@link #createSplashScreen()} - a splash screen
 *       while the application starts (logo, scanner bar, colours, jingle). Without them the
 *       application starts without one.</li>
 *   <li>{@link #isJvmExitOnStop()} - the application embeds Swing (the code editor of the
 *       framework, or components of its own): the AWT event dispatch thread is not a daemon
 *       thread and would keep the JVM alive after the JavaFX runtime is gone.</li>
 * </ul>
 *
 * @see LicensedApplication
 */
public abstract class BaseApp extends Application implements LicensedApplication, InstallEndListener {
	
	/**
	 * Logger.
	 */
	protected static final Logger LOG = LoggerFactory.getLogger(BaseApp.class.getName());

    /** System property that overrides the configured language, e.g. {@code -Dapp.lang=de}. */
    private static final String PROP_LANG = "app.lang";

    /** System property that switches the development-only CSS live reload on, e.g. {@code -Dapp.livereload=true}. */
    private static final String PROP_LIVERELOAD = "app.livereload";

    /** Legacy alias of {@link Constants#PROP_KEY_LANGUAGE}. */
    private static final String PROP_LANG_ALIAS = "lang";

    /** Initial font size */
    private static final int FONT_INIT_SIZE = 14;

    /** Show uncaught exceptions? */
    private static boolean showUncaughtExceptions = true;
    
	/**
	 * Debug file logger; use -DfileDebug=true.
	 */
    private static PrintWriter DEBUG_LOG_WRITER = null;
	
    /**
     * Debug log date-time formatter.
     */
    private static DateTimeFormatter DEBUG_LOG_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private static final List<File> recentFiles = new ArrayList<>();
    
    private Stage primaryStage = null;
    private Properties config = null;

    /**
     * The splash screen of the application, if it has one. It is kept here so that it can be
     * closed when the application ends - an open splash screen would keep the application alive.
     */
    private SplashScreen splashScreen = null;
    /**
     * Configuration parameters set at runtime, e.g. by the installer steps or by a
     * language switch. They live in memory (like the recent files) and are written with
     * the next {@link #storeProps()} - and with the configuration the installation
     * creates, see {@link #completed(ProcessException)}.
     */
    private final Properties changedConfig = new Properties();
    
    private boolean licenseAccepted = false;

    
    /**
     * File extensions an application allows to be opened from the command line, each with a
     * leading dot in lower case. Empty by default; the application registers its own endings via
     * {@link #registerOpenExtension(String)}.
     */
    private final Set<String> openExtensions = new LinkedHashSet<>();

    /**
     * The base configuration file ('cfg/base.cfg').
     */
    protected String baseCfgFile = null;
    
    /**
     * The base configuration properties.
     */
    protected Properties baseProps = null;
    
    
    /**
     * Default constructor.
     */
    public BaseApp() {
    }
    
	// ---------------- Overrides ----------------
	
	@Override
	public void init() throws Exception {
		final String fileDebug = System.getProperty("fileDebug");
		if (fileDebug != null && Boolean.parseBoolean(fileDebug)) {
			String appName = this.getInternalAppName();
			if (appName == null) {
				appName = "autumo-ui-fx";
			}
	        try {
	        	DEBUG_LOG_WRITER = new PrintWriter(new FileWriter(System.getProperty("user.home") + OSUtils.FILE_SEPARATOR + appName + "-debug.log", true), true);
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
		}
		System.setProperty("apple.awt.application.name", this.getAppTitle());
	}
	
	@Override
	public void start(Stage primaryStage) {

		// Set manufacturer
		System.setProperty(Constants.SYS_KEY_SYSTEM_MANUFACTURER, this.getManufacturer());
		
    	// We must pseudo-pre-load the configuration manager
		// for the pre-initialization stuff!
		final Properties initProps = Helper.loadTemplateConfiguration(this);
    	try {
			ConfigurationManager.getInstance().initializeWithProperties(initProps);
		} catch (Exception e) {
		}
		
    	// Protocols: for once and all...
		if (System.getProperty("https.protocols") == null) {
			String prots = ConfigurationManager.getInstance().getString("https.protocols", "TLSv1.2,TLSv1.3");
			prots = prots.trim().replace(" ", "");
			System.setProperty("https.protocols", prots);
		}
		if (System.getProperty("jdk.tls.client.protocols") == null) {
			String prots = ConfigurationManager.getInstance().getString("jdk.tls.client.protocols", "TLSv1.2,TLSv1.3");
			prots = prots.trim().replace(" ", "");
			System.setProperty("jdk.tls.client.protocols", prots);
		}		
		
		final Font defaultFont = Font.loadFont(Helper.getBaseResource("fonts/Inter-Regular.ttf"), FONT_INIT_SIZE);
		FontRegistry.setDefault(defaultFont);
		FontRegistry.register(defaultFont);
		FontRegistry.register(Font.loadFont(Helper.getBaseResource("fonts/Inter-Italic.ttf"), FONT_INIT_SIZE));
		FontRegistry.register(Font.loadFont(Helper.getBaseResource("fonts/Inter-Bold.ttf"), FONT_INIT_SIZE));
		FontRegistry.register(Font.loadFont(Helper.getBaseResource("fonts/Inter-BoldItalic.ttf"), FONT_INIT_SIZE));
		FontRegistry.register(Font.loadFont(Helper.getBaseResource("fonts/Inter-Medium.ttf"), FONT_INIT_SIZE));
		FontRegistry.register(Font.loadFont(Helper.getBaseResource("fonts/Inter-MediumItalic.ttf"), FONT_INIT_SIZE));
		FontRegistry.register(Font.loadFont(Helper.getBaseResource("fonts/Inter-SemiBold.ttf"), FONT_INIT_SIZE));
		FontRegistry.register(Font.loadFont(Helper.getBaseResource("fonts/Inter-SemiBoldItalic.ttf"), FONT_INIT_SIZE));
		
		this.startup(primaryStage);
		
		if (!this.isPortableApp()) {
	    	try {
				LoggingFactory.getInstance().initializeInMemoryFile(this.getInMemoryConfigName(), this.getInternalAppName());
			} catch (Exception e) {
				System.err.println("autumo UI: Logging configuration failed! Exception: " + e.getMessage());
			}
		}
		
    	// Application icon: the application's own icons if the application ships any,
    	// otherwise the default autumo-ui logo - on all operating systems.
		primaryStage.setTitle(Helper.getMessageBundle(this.getBaseResourcePath()).getString("application.name"));
		this.applyApplicationIcon(primaryStage);

        this.primaryStage = primaryStage;

        // The configuration is read before the splash screen: the splash screen may depend on it
        // (a jingle the user switched off, for example).
        final Properties configuration = this.loadConfiguration();
        
        // Show uncaughr exceptions?
        if (configuration == null) {
    		showUncaughtExceptions = ConfigurationManager.getInstance().getYesOrNoNoWarn("showUncaughtExceptions");
        } else {
        	showUncaughtExceptions = configuration.getProperty("showUncaughtExceptions", "yes").trim().toLowerCase().equals("yes");
        }
    	// Default exception handler
        Thread.currentThread().setUncaughtExceptionHandler(BaseApp::handleUncaughtException);
        

        // The splash screen of the application: none by default (isSplashScreenEnabled()). It
        // comes up first - the jingle starts with it, the window becomes visible a moment later,
        // when the JavaFX thread is free again. If installer is needed, no splash is shown.
        final SplashScreen splash = this.isSplashScreenEnabled() && configuration != null ? this.createSplashScreen() : null;
        this.splashScreen = splash;
        if (splash != null) {
        	splash.show();
        	// The splash screen must not keep the application alive: it goes away with the main
        	// window.
        	primaryStage.showingProperty().addListener((_, _, showing) -> {
        		if (!showing) {
        			this.hideSplash();
        		}
        	});
        }

        // The view is built and the main window shown; the splash screen goes away with it - or
        // after its minimum display time.
        this.buildApplication(primaryStage, configuration, splash);
	}

	/**
	 * Handle for uncaught exceptions.
	 * 
	 * @param thread current therad
	 * @param ex exception
	 */
	public static void handleUncaughtException(Thread thread, Throwable ex) {
		LOG.error("Uncaught Exception", ex);
		if (showUncaughtExceptions) {
			UI.showExceptionDialog(ex);
		}
	}
	
	/**
	 * Reads the configuration of the application and loads it into the configuration manager.
	 *
	 * <p>Portable applications read the template configuration, installed applications the
	 * configuration in the user's application data directory. An installed application that has
	 * no configuration yet is not installed yet either: the method then gives {@code null} and
	 * the installer runs.</p>
	 *
	 * @return the configuration or {@code null} if the application has to be installed
	 */
	private Properties loadConfiguration() {

     	File f = null;
     	Properties p = null;
     	if (this.isPortableApp()) {
			p = Helper.loadTemplateConfiguration(this);
    	} else {
    		// Default: Already stored config?
    		f = new File(OSUtils.getPropertiesPath(this.getInternalAppName()) + Constants.FILE_INTERNAL_CONF);
    	}

        if (!((f != null && f.exists()) || p != null)) {
        	// Nothing stored yet: the application has to be installed.
        	return null;
        }

    	if (this.isPortableApp()) {
    		this.config = p;
    	} else {
    		// Default
    		this.config = Helper.loadAppConfiguration(this);
    	}

        // Enrich configuration manager with finished config
    	try {
    		ConfigurationManager.getInstance().initializeWithProperties(this.config);
    	} catch (Exception e) {
    	}

        this.loadRecentFiles(this.config);
        return this.config;
	}

	/**
	 * Builds the view of the application and shows it.
	 *
	 * <p>Runs on the JavaFX thread. The main window comes up as soon as it can - nothing is
	 * delayed for the splash screen, and the splash screen is created before the view of the
	 * application is built, so it is up while the application loads. It stays on top of the main
	 * window for the time it was given and then goes away; without a time it goes away with the
	 * main window.</p>
	 *
	 * @param primaryStage  primary window
	 * @param configuration the configuration, or {@code null} if the application has to be installed
	 * @param splash        the splash screen, or {@code null} if the application has none
	 */
	private void buildApplication(Stage primaryStage, Properties configuration, SplashScreen splash) {
		if (configuration == null) {
			// The application has to be installed: the installer comes instead of the main window.
			try {
				this.preInitialize(primaryStage, this.config);
		        // Resolve the language: -Dapp.lang > cfg language/lang > desktop language > English.
		        final String lang = resolveLanguage(configuration);
		        Messages.setLanguage(lang, getBaseResourcePath());
		        
				final InstallAction installer = new InstallAction(this);
				installer.run();
			} finally {
				if (splash != null) {
					splash.hide();
				}
			}
			return;
		}

		final Pane view = this.preInitialize(primaryStage, configuration);
        // Resolve the language: -Dapp.lang > cfg language/lang > desktop language > English.
        final String lang = resolveLanguage(configuration);
        Messages.setLanguage(lang, getBaseResourcePath());
        
		this.installCssLiveReload();
		
		// Install Default Shortcuts
		Method getS = null;
		Class<?> clz = view.getClass();
		try {
			if (BaseView.class.isAssignableFrom(clz))
				getS = BaseView.class.getDeclaredMethod("getController");
			if (BaseBorderView.class.isAssignableFrom(clz))
				getS = BaseBorderView.class.getDeclaredMethod("getController");
			if (BaseAnchorView.class.isAssignableFrom(clz))
				getS = BaseAnchorView.class.getDeclaredMethod("getController");
		} catch (NoSuchMethodException e) {
			LOG.error("UI: 'getController' method not found in class '"+clz+"'; the view must be sub-classed by a valid base view!", e);
			throw new RuntimeException("UI: 'getController' method not found in class '"+clz+"'; the view must be sub-classed by a valid base view!", e);
		}
		final BaseController controller;
		try {
			controller = (BaseController) getS.invoke(view);
		} catch (Exception e) {
			LOG.error("UI: Couldn't invoke 'getController' on class '"+clz+"' !", e);
			throw new RuntimeException("UI: Couldn't invoke 'getController' on class '"+clz+"'; the view must be sub-classed by a valid base view!", e);
		}
		
        // The red button ends the application: on macOS it only hides the window (the process
        // would stay alive in the dock), and the close request is not even fired there. The
        // splash screen has switched the implicit exit off (a window must not end the
        // application), so the application ends itself here.
        primaryStage.showingProperty().addListener((_, _, showing) -> {
            if (showing) {
                return;
            }
            // Unsaved changes would be lost: ask before the window goes away. On macOS the
            // window is already hidden at this point, so it is shown again for the dialog -
            // and ends the application right away when the user confirms.
            if (controller != null && !controller.confirmDiscardChanges()) {
                primaryStage.show();
                return;
            }
            Platform.exit();
        });
        primaryStage.setOnCloseRequest(event -> {
            // On platforms that do fire the close request the guard runs before the window hides.
            if (controller != null && !controller.confirmDiscardChanges()) {
                event.consume();
            }
        });
        
		// The main window comes as fast as it can - nothing is delayed for the splash screen.
		this.initialize(primaryStage, configuration);

		// Two modes, and they are independent of when the splash screen is shown:
		//   - with a time: the splash screen stays on top for that time and then fades out.
		//   - without a time: the splash screen goes away with the main window.
		final Duration remaining = splash == null ? Duration.ZERO : splash.remainingDuration();
		if (remaining.greaterThan(Duration.ZERO)) {
			final PauseTransition wait = new PauseTransition(remaining);
			wait.setOnFinished(_ -> hideSplash());
			wait.play();
			return;
		}
		hideSplash();
	}

	/**
	 * Hides the splash screen and forgets it: an open splash screen would keep the application
	 * alive.
	 */
	private void hideSplash() {
		if (this.splashScreen != null) {
			this.splashScreen.hide();
			this.splashScreen = null;
		}
	}

	/**
	 * Whether the application shows a splash screen while it starts.
	 *
	 * <p>The default is {@code false}: an application without a splash screen does not get
	 * one. An application that wants one overrides this method and gives {@code true}; the
	 * screen itself comes from {@link #createSplashScreen()}.</p>
	 *
	 * @return {@code true} if the application shows a splash screen
	 */
	protected boolean isSplashScreenEnabled() {
		return false;
	}

	/**
	 * Creates the splash screen of the application.
	 *
	 * <p>The default is the standard design with the framework's logo. An application overrides
	 * this method and sets its own logo, its colours and - if it wants one - a jingle:</p>
	 *
	 * <pre>
	 * &#64;Override
	 * protected SplashScreen createSplashScreen() {
	 *     return new SplashScreen()
	 *             .setLogo(getClass().getResource("icons/app-256.png"))
	 *             .setJingle(getClass().getResource("sounds/start.wav"));
	 * }
	 * </pre>
	 *
	 * @return the splash screen of the application
	 */
	protected SplashScreen createSplashScreen() {
		return new SplashScreen();
	}

	/**
	 * Starts the development-only CSS live reload, if it was switched on explicitly.
	 * 
	 * <p>The parameter {@code -Dapp.livereload=true} on the command line turns it on; without it
	 * nothing happens (no thread, no temp directory). It watches the application's own
	 * {@code style/application.css} and only works in a development layout (an unpackaged
	 * {@code file:} URL) - in a packaged application it is a no-op. See
	 * {@link CssLiveReload#install(javafx.scene.Scene, String, String)}.</p>
	 */
	private void installCssLiveReload() {
		if (!Boolean.parseBoolean(System.getProperty(PROP_LIVERELOAD, "false")))
			return;
		final Scene scene = this.primaryStage.getScene();
		if (scene == null)
			return;
		try {
			CssLiveReload.install(scene, Helper.getAppResource(this, Constants.STYLESHEET), getInternalAppName());
		} catch (Exception e) {
			LOG.warn("Could not install the CSS live reload: {}", e.getMessage());
		}
	}

	/**
	 * Applies the application icon to the primary stage: the application's own icons
	 * if the application ships any, otherwise the default autumo-ui logo.
	 * 
	 * <p>See {@link Helper#getApplicationIcons(BaseApp)} for the icon convention. The
	 * icons are applied on all operating systems: they are the window and task-bar
	 * icon on Windows and Linux. On macOS the dock icon of a packaged application is
	 * defined by the application package (jpackage {@code --icon}, {@code .icns}),
	 * because JavaFX stage icons do not reach the macOS dock.</p>
	 * 
	 * @param primaryStage primary stage
	 */
	private void applyApplicationIcon(Stage primaryStage) {
		for (URL iconUrl : Helper.getApplicationIcons(this)) {
			try {
				primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
			} catch (Exception e) {
				LOG.warn("Could not load application icon '{}'!", iconUrl, e);
			}
		}
	}

	/**
	 * Called before everything else, overwrite if necessary.
	 * 
	 * @param primaryStage primary stage
	 */
	protected void startup(Stage primaryStage) {
	}

    /**
	 * <p>Portable applications do not log, install or write any other files on the host;
	 * only the template configuration is read! Portable applications cannot be
	 * licensed either.</p>
	 * 
	 * <p>Overwrite for portable applications.</p>
	 * 
     * <p><b>This is the switch that writes {@code cfg/app.cfg}.</b> An installed application has no
     * application configuration in the user's application data directory on its first start, so the
     * framework hands {@code null} to
     * {@link #preInitialize(Stage, Properties)} and runs the installer instead - the steps listed
     * in {@code <app resource path>/cfg/installer-steps.cfg}, the last of which calls
     * {@link ch.autumo.ui.fx.BaseApp#completed(ProcessException)}. That writes the application
     * configuration into the user's application data directory
     * ({@code <app data>/<vendor>/<app internal name>/cfg/app.cfg}) and calls
     * {@link #initialize(Stage, Properties)} afterwards.</p>
     *
     * <p>From then on that file is the configuration: the general values of the bundled template
     * {@code <app resource path>/cfg/app.cfg} plus every user setting - the recent files,
     * {@code language} and the values the installer steps collect via
     * {@link ch.autumo.ui.fx.BaseApp#setConfigurationParameter(String, String)}. The template
     * itself is never touched; it is the general configuration the file is created from.</p>
     *
     * @return {@code false}: the application is installed, not portable
     */
	protected boolean isPortableApp() {
		return false;
	}
	
	@Override
	public void stop() throws Exception {
		// The splash screen must not keep the application alive: it goes away with it.
		this.hideSplash();
        this.storeProps();
        this.finish(this.primaryStage);
        // The JavaFX runtime is gone, but the JVM can still be held by other threads - the AWT
        // event dispatch thread of the embedded Swing editor, for example. An application that
        // uses the editor says so (isJvmExitOnStop()).
        if (this.isJvmExitOnStop()) {
        	System.exit(0);
        }
	}

	/**
	 * Whether the JVM ends when the application stops.
	 *
	 * <p><b>When is this needed?</b> After {@link javafx.application.Platform#exit()} the JavaFX
	 * runtime is gone - but the JVM can still be held by other threads. The AWT event dispatch
	 * thread is one of them: it is <b>not</b> a daemon thread, and it exists as soon as the
	 * application uses Swing. That is the case for</p>
	 * <ul>
	 *   <li>the code editor of the framework ({@link DefaultCodeEditorView}) and with it the
	 *       JSON formatter pane,</li>
	 *   <li>any Swing component an application embeds itself - the framework's
	 *       {@code SwingNode} or the application's own.</li>
	 * </ul>
	 * <p>On macOS the AWT thread can even block for ever in a native call while a Swing
	 * component is removed, so the JVM does not end by itself in that case either. An application
	 * that uses Swing should therefore end the JVM itself and return {@code true} here. An
	 * application without any Swing component needs nothing: after {@code Platform.exit()} the JVM
	 * ends by itself.</p>
	 *
	 * <p>The default is {@code false}: the framework does not end the JVM of an application
	 * behind its back - only an application that knows that it embeds Swing does.</p>
	 *
	 * @return {@code true} if the JVM ends with the application
	 */
	protected boolean isJvmExitOnStop() {
		return false;
	}
    
    /**
     * Store configuration properties to disk.
     * 
     * <p>Persists what the running application changed in memory: the parameters set with
     * {@link #setConfigurationParameter(String, String)} and the recent files. The
     * framework calls this when the application exits; an application can call it earlier
     * when a setting has to survive immediately.</p>
     */
    public void storeProps() {
    	this.storeProps(null);
    }
	
    @Override
    public void completed(ProcessException ex) {
    	
    	if (this.isPortableApp())
    		return;
    	
        final File f = new File(OSUtils.getPropertiesPath (this.getInternalAppName()) + Constants.FILE_INTERNAL_CONF);
        this.config = new Properties();

        // create folders for property file
        new File(f.getParent()).mkdirs();

        if (licenseAccepted)
        	this.config.setProperty(Constants.PROP_KEY_LIC_ACC, Constants.DATE_FORMAT_LIC_EXT.format(new Date()));
        
        // Initially load application template configuration!
        this.config = GeneralUtils.copyProperties(this.config, Helper.loadTemplateConfiguration(this), true);
        // Parameters that were changed in memory (installer steps) belong into the file
        this.config = GeneralUtils.copyProperties(this.config, this.changedConfig, true);
        // Store it to final destination as application configuration
        Helper.storeAppConfiguration(this, this.config, this.getConfigComment());            
        this.changedConfig.clear();
        
        // Enrich configuration manager with finished config
    	try {
			ConfigurationManager.getInstance().initializeWithProperties(this.config);
		} catch (Exception e) {
		}
        
        this.initialize(this.primaryStage, this.config);
    }  

	/**
     * Get a configuration parameter/value.
     * 
     * <p>Returns the value of the application configuration; a parameter that was changed
     * at runtime ({@link #setConfigurationParameter(String, String)}) is returned from
     * memory as long as it has not been stored yet.</p>
     * 
     * @param key key
     * @return value or null
     */
    public String getConfigurationParameter(String key) {
    	if (this.config != null) {
    		final String value = this.config.getProperty(key);
    		if (value != null)
    			return value;
    	}
    	return this.changedConfig.getProperty(key);
    }

    /**
     * Set an application configuration parameter in memory.
     *
     * <p>The value is the one {@link #getConfigurationParameter(String)} returns
     * afterwards. Like the recent files it is written to the application configuration
     * ('cfg/app.cfg') with the next {@link #storeProps()} - the exit of the application
     * stores it, and so does every place that wants a setting to survive immediately -
     * and with the file the installation writes ({@link #completed(ProcessException)}).</p>
     *
     * <p>Nothing is written when the application has no configuration on disk yet, e.g.
     * in {@link #isPortableApp() portable mode}; the value then only lives in the
     * running application.</p>
     *
     * @param key parameter key
     * @param value parameter value
     */
    public void setConfigurationParameter(String key, String value) {
    	if (this.config == null)
    		this.config = new Properties();
    	this.config.setProperty(key, value);
    	this.changedConfig.setProperty(key, value);
    }
    
    /**
     * Get primary stage.
     * 
     * @return primary stage
     */
    public Stage getPrimaryStage() {
        return this.primaryStage;
    }

    /**
     * Registers a file extension the application allows to be opened from the command line (and,
     * by convention, from a double-click / <em>Open With</em> association). Without any
     * registration the framework never treats a bare command-line argument as a file to open.
     *
     * <p>Call this from the application's constructor or {@link #init()}; a leading dot is
     * optional and the value is matched case-insensitively, e.g. {@code registerOpenExtension("ifcx")}
     * or {@code registerOpenExtension(".ifcx")}.</p>
     *
     * @param extension file extension, with or without a leading dot; {@code null}, blank or a
     *                  bare dot is ignored
     */
    public final void registerOpenExtension(String extension) {
        if (extension == null)
            return;
        final String ext = extension.trim().toLowerCase(Locale.ROOT);
        if (ext.isEmpty() || ".".equals(ext))
            return;
        this.openExtensions.add(ext.startsWith(".") ? ext : "." + ext);
    }

    /**
     * Get the file extensions the application allows to be opened from the command line, each with
     * a leading dot in lower case.
     *
     * @return an unmodifiable view of the registered extensions, in registration order
     */
    public final Set<String> getOpenExtensions() {
        return Collections.unmodifiableSet(this.openExtensions);
    }

    /**
     * Picks the file to open from the raw command line arguments, honouring the extensions the
     * application registered via {@link #registerOpenExtension(String)}.
     *
     * <p>Recognises {@code --open <file>} (any path) and a bare {@code <file>} whose extension was
     * registered. Use this in {@link #initialize(Stage, Properties)} to honour a
     * double-clicked file.</p>
     *
     * @param args raw command line arguments (may be {@code null} or empty)
     * @return the file to open, or {@code null} when the command line carries none
     */
    public final Path openTargetFromCommandLine(List<String> args) {
        return CommandLine.openTarget(args, this.openExtensions);
    }

    /**
     * Load a configuration parameter directly from configuration file.
     * 
     * @param key key for value
     * @return value if any, otherwise null
     */
    public String loadConfigurationParamaterFromFile(String key) {
    	return Helper.loadAppConfiguration(this).getProperty(key);
    }

    /**
     * Remove app configuration from disk.
     */
    public void removeConfig() {
        final File propRootDir = new File(Helper.getPropertiesPath(this));
        OSUtils.deleteFiles(new File[] {propRootDir});
        propRootDir.delete();
    }   
    
    /**
     * Set license accepted flag.
     */
	public final void licenseAccepted() {
        licenseAccepted = true;
	}

    /**
     * Get license accepted flag.
     * @return true, if license was accepted by the user during install 
     */
	public final boolean isLicenseAccepted() {
        return licenseAccepted;
	}
	
    /**
     * Checks if this app has been installed before; Installer has been run.
     * 
     * @return true if app is installed
     */
    public boolean isInstalled () {
    	final File f = new File(Helper.getPropertiesPath(this) + Constants.FILE_INTERNAL_CONF);
    	return f.exists();
    }
    
	/**
	 * Load recent used files.
	 * 
	 * @param p configuration properties
	 */
    public void loadRecentFiles(Properties p) {
        for (int i = 0; i < Constants.RECENT_MAX_SIZE; i++) {
            final String currRecentFilesStr = p.getProperty(Constants.PROP_KEY_RECENT + i, "");
            if (!currRecentFilesStr.isEmpty()) {
                final File file = new File(currRecentFilesStr);
                if (file.exists()) {
                    recentFiles.add(file);
                }
            }
        }
    }

    /**
     * Clear recent used file list.
     */
    public void clearRecentFiles() {
        recentFiles.clear();
    }
    
    /**
     * Get recent used files.
     * 
     * @return recent files
     */
    public List<File> getRecentFiles() {
        return recentFiles;
    }

    /**
     * Add a recent used file.
     * 
     * @param file recent file
     */
    public void addRecentFile(File file) {
        final int idx = recentFiles.indexOf(file);
        if (idx != -1) {
            recentFiles.remove(idx);
            recentFiles.add(0, file);
            return;
        }
        
        if (recentFiles.size() == Constants.RECENT_MAX_SIZE) {
            recentFiles.remove(Constants.RECENT_MAX_SIZE - 1);
        }
        recentFiles.add(0, file);
    }
    
    /**
     * Remove a file from recent used file list.
     * 
     * @param file file to remove
     * @return true if removed, false otherwise
     */
    public boolean removeRecentFile(File file) {
    	if (file == null)
    		return false;
        final int idx = recentFiles.indexOf(file);
        if (idx != -1) {
            recentFiles.remove(idx);
            return true;
        }
        return false;
    }

    /**
     * Get initial fallback value for the configuration file by 
     * specified configuration ID. This is only used if every other
     * method failed; e.g. missing template configuration.
     * 
     * @param id configuration ID
     * @return configuration value
     */
	public String getInitConfidParameter(String id) {
		switch (id) {
			case ch.autumo.ui.Constants.CONFIG_ENTRY_URL_PURCHASE:
				return Constants.DEF_CONFIG_ENTRY_URL_PURCHASE;
			case ch.autumo.ui.Constants.CONFIG_ENTRY_URL_WEB:
				return Constants.DEF_CONFIG_ENTRY_URL_WEB;
			case ch.autumo.ui.Constants.CONFIG_ENTRY_URL_DOC:
				return Constants.DEF_CONFIG_ENTRY_URL_DOC;
			case ch.autumo.ui.Constants.CONFIG_ENTRY_URL_WEB_UPDATE:
				return Constants.DEF_CONFIG_ENTRY_URL_WEB_UPDATE;
			case ch.autumo.ui.Constants.CONFIG_ENTRY_URL_WEB_VER_FILE:
				return Constants.DEF_CONFIG_ENTRY_URL_WEB_VER_FILE;
			case ch.autumo.ui.Constants.CONFIG_ENTRY_EMAIL_SALES:
				return Constants.DEF_CONFIG_ENTRY_EMAIL_SALES;
			case ch.autumo.ui.Constants.CONFIG_ENTRY_EMAIL_SUPPORT:
				return Constants.DEF_CONFIG_ENTRY_EMAIL_SUPPORT;
			default:
				return "null";
		}
	}
	
    /**
     * Get configuration template file.
     * 
     * @return configuration template file
     */
    public abstract String getConfigTemplateFile();
    
    
	// ---------------- protected methods ----------------

    /**
     * Manufacturer of this application. We suggest not using 
     * spaces for this information; it is used to group the
     * application's data on the desktop in a directory where
     * all application data of a certain manufacturer is located.
     * 
     * Overwrite this and use your own enterprise/company name.
     * 
     * Typical locations on systems:
     * - Linux   : /home/USER/.APPNAME (no manufacturer name used)
     * - macOS   : /Users/USER/Library/Application Support/MANUFACTURER/APPNAME
     * - Windows : C:\Users\USER\AppData\Roaming\MANUFACTURER/APPNAME
     *  
     * @return manufacturer name
     */
	protected String getManufacturer() {
		return "autumo";
	}
	
    /**
     * Pre-initialize the FX GUI for the installer.
     * Create the main view here and call UI.setup(...).
     * 
     * Run before the installer if any.
     * 
     * @param primaryStage primary stage
     * @param configuration loaded configuration properties
     * @return the main view
     */
    protected abstract Pane preInitialize(Stage primaryStage, Properties configuration);
    
    /**
     * Initialize the FX GUI - {@link #preInitialize(Stage, Properties)} must 
     * have been called before this method! This will show the 
     * main view created previously.
     * 
     * Run after the installer if any.
     * 
     * @param primaryStage primary stage
     * @param configuration loaded configuration properties
     */
    protected abstract void initialize(Stage primaryStage, Properties configuration);

    /**
     * Get base resource path for application.
     * 
     * @return base resource path for application
     */
	public abstract String getBaseResourcePath();

    /**
     * Get in-memory configuration name.
     * 
     * @return in-memory configuration name
     */
	public abstract String getInMemoryConfigName();

    /**
     * Get application version.
     * It is also used for online upgrade checks, if applied.
     * 
     * @return application version
     */
	public abstract String getAppVersion();
	
    /**
     * Get application title.
     * Part of the online license check, if applied.
     * 
     * @return application title
     */
	public abstract String getAppTitle();
	
    /**
     * Get an internal application name; make sure it is one word
     * with small and/or big letters and no space.
     * 
     * It is used for internal purposes, e.g. configuration directory.
     * 
     * @return application name
     */
	public abstract String getInternalAppName();

    /**
     * Finish program.
     * 
     * @param primaryStage primary stage
     */
    public abstract void finish(Stage primaryStage);

    
	// ---------------- Private methods ----------------
    
    /**
     * Get the default comment for the file configuration.
     * 
     * @return comment
     */
    private final String getConfigComment() {
        return this.getAppTitle() + " " + this.getAppVersion() + " - General Settings";
    }
	
    /**
     * Store configuration properties to disk.
     * 
     * @param newProps configuration properties
     */
    private void storeProps(Properties newProps) {
    	Properties p = null;
        final File f = new File(OSUtils.getPropertiesPath(this.getInternalAppName()) + Constants.FILE_INTERNAL_CONF);
        // only if app data hasn't been removed!
        if (f.exists()) {

        	p = Helper.loadAppConfiguration(this);
            // merge new props
            if (newProps != null)
            	p = GeneralUtils.copyProperties(p, newProps, true);
            // parameters changed at runtime are stored like this, too, see setConfigurationParameter()
            if (this.changedConfig.size() > 0)
            	p = GeneralUtils.copyProperties(p, this.changedConfig, true);
            
            this.collectRecentFiles(p);
            Helper.storeAppConfiguration(this, p, this.getConfigComment());
            this.changedConfig.clear();
            this.config = p;
        }
    }
    
    /**
     * Store recent files to configuration properties
     * in memory.
     * 
     * @param p configuration properties
     */
    private void collectRecentFiles(Properties p) {
        // first reset all, maybe list has been cleared
        for (int i = 0; i < Constants.RECENT_MAX_SIZE; i++) {
            p.setProperty(Constants.PROP_KEY_RECENT + i, "");
        }
        for (int i = 0; i < recentFiles.size(); i++) {
            final File currFile = recentFiles.get(i);
            p.setProperty(Constants.PROP_KEY_RECENT + i, currFile.getAbsolutePath());
        }
    }
	
    // Helper methods
	
    /**
     * Checks whether a language configuration value is absent or only whitespace; such a value
     * does not take part in the precedence and the next candidate is used instead.
     *
     * @param value value to check, may be {@code null}
     * @return {@code true} when the value carries nothing
     */
    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
	
    /**
     * Resolves the language an application starts in.
     *
     * <p>In order: the {@code -Dapp.lang} system property, the {@code language} key of the
     * application configuration ({@link Constants#PROP_KEY_LANGUAGE}) and its {@code lang} alias,
     * then the language of the desktop ({@link Locale#getDefault()}, which the JVM filled from the
     * operating system — e.g. {@code de_CH} on a Swiss German desktop).</p>
     *
     * <p>The result is not checked here: {@link Helper#setLanguage(String)} normalises a region
     * away ({@code de_CH} → {@code de}) and maps an unsupported or blank value — including a
     * desktop language the application has no PO catalogue for — to English.</p>
     *
     * @param configuration application configuration, may be {@code null}
     * @return the resolved language code, e.g. {@code de}
     */
    public static String resolveLanguage(Properties configuration) {
        // No configuration at all: the first start of an installed application, where the
        // framework runs the installer before it writes cfg/app.cfg (see isPortableApp()).
        final Properties config = configuration == null ? new Properties() : configuration;
        String lang = System.getProperty(PROP_LANG);
        if (isBlank(lang)) {
            lang = config.getProperty(Constants.PROP_KEY_LANGUAGE);
        }
        if (isBlank(lang)) {
            lang = config.getProperty(PROP_LANG_ALIAS);
        }
        if (isBlank(lang)) {
            lang = Locale.getDefault().getLanguage();
        }
        return lang;
    }
    
    /**
     * Log debug message if debug file logger is activated. 
     * Use <code>-DfileDebug=true</code> for activating debug file log
     * (Log file is written to user home directory).
     * 
     * @param message message
     */
    public static void log(String message) {
        if (DEBUG_LOG_WRITER != null) {
        	DEBUG_LOG_WRITER.println(LocalDateTime.now().format(DEBUG_LOG_FORMATTER) + " - " + message);
        }
    }
    
    /**
     * Log debug exception if debug file logger is activated.
     * Use <code>-DfileDebug=true</code> for activating debug file log
     * (Log file is written to user home directory).
     * 
     * @param ex exception
     */
    public static void log(Exception ex) {
        if (DEBUG_LOG_WRITER != null) {
            DEBUG_LOG_WRITER.println(LocalDateTime.now().format(DEBUG_LOG_FORMATTER) + " - Exception ->");
            ex.printStackTrace(DEBUG_LOG_WRITER);
        }
    }
	
}
