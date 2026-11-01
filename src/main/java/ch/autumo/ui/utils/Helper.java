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

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.autumo.ui.Constants;
import ch.autumo.ui.data.CountryData;
import ch.autumo.ui.fx.BaseApp;
import javafx.scene.Cursor;
import javafx.scene.ImageCursor;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * Helper.
 */
public class Helper {

	private final static Logger LOG = LoggerFactory.getLogger(Helper.class.getName());
    private static final int MAX_FILE_EXTENSION_CHAR_COUNT = 10;

    /** Currently selected language (ISO 639-1), lazily derived from 'user.language'. */
    private static String language = null;
    /** Listeners that are notified after the language changed; see {@link #addLanguageListener(Runnable)}. */
    private static final List<Runnable> LANGUAGE_LISTENERS = new CopyOnWriteArrayList<>();
    /**
     * Cache of the message bundles, keyed by base resource path and language. Building a bundle
     * parses the PO catalogue of the application, which must not happen on every text lookup.
     * Cleared on a language change in {@link #setLanguage(String)}.
     */
    private static final Map<String, ResourceBundle> MESSAGE_BUNDLES = new ConcurrentHashMap<>();

    
	/**
	 * Default constructor.
	 */
	private Helper() {
	}
	
	/**
     * Load configuration properties / app settings.
     * 
     * @param app the application
     * @return configuration properties
     */
    public static Properties loadAppConfiguration(BaseApp app) {
    	Properties p = new Properties();
    	final File f = new File(OSUtils.getPropertiesPath(app.getInternalAppName()) + Constants.FILE_INTERNAL_CONF);
    	FileInputStream fis = null; 
        try {
        	fis = new FileInputStream(f);
            p.load(fis);
            // Also load the configuration manager for convenience
            ConfigurationManager.getInstance().initializeWithProperties(p);
        } catch (Exception ex) {
        	LOG.error("Loading user settings failed, loading template settings!", ex);
        	p = loadTemplateConfiguration(app);
        	try {
                // Also load the configuration manager for convenience
				ConfigurationManager.getInstance().initializeWithProperties(p);
			} catch (Exception e) {
	        	LOG.error("Configuration manager setup failed!", ex);
			}
        } finally {
        	if (fis != null)
				try {
					fis.close();
				} catch (IOException e) {
				}
		}
        
        return p;
    }
 
    /**
     * Store application configuration 'app.cfg'.
     * 
     * @param app the application
     * @param settings the settings
     * @param comment the settings
     */
    public static void storeAppConfiguration(BaseApp app, Properties settings, String comment) {
    	final File f = new File(OSUtils.getPropertiesPath(app.getInternalAppName()) + Constants.FILE_INTERNAL_CONF);
	    FileOutputStream fos = null;
	    try {
	    	fos = new FileOutputStream(f);
	    	if (comment == null)
	    		comment = app.getAppTitle() + " " + app.getAppVersion() + " - General Settings";
		    settings.store(fos, comment);
		    fos.flush();
	    } catch (IOException ex) {
	    	LOG.error("Storing user settings failed!", ex);
	    } finally {
	    	if (fos != null)
				try {
					fos.close();
				} catch (IOException e) {
				}
	    }
    }

    /**
     * Get base configuration/settings path for the application.
     * 
     * @param app the application
     * @return the base configuration directory
     */
    public static String getPropertiesPath(BaseApp app) {
    	return OSUtils.getPropertiesPath(app.getInternalAppName());
    }
    
    /**
     * Load template configuration properties 'app.cfg'
     * 
     * @param app the application
     * @return configuration properties
     */
    public static Properties loadTemplateConfiguration(BaseApp app) {
    	final Properties internalConfig = new Properties();
        try {
            internalConfig.load(app.getClass().getResourceAsStream(app.getConfigTemplateFile()));
        } catch (Exception ex) {
        	ex.printStackTrace();
        	// This is only the case if the application has not an internal template configuration!
        	// It will be filled with MINIMUM standard information!
            LOG.error("Couldn't load internal config file!", ex);
            internalConfig.put(Constants.CONFIG_ENTRY_URL_PURCHASE, app.getInitConfidParameter(Constants.CONFIG_ENTRY_URL_PURCHASE));
            internalConfig.put(Constants.CONFIG_ENTRY_URL_WEB, app.getInitConfidParameter(Constants.CONFIG_ENTRY_URL_WEB));
            internalConfig.put(Constants.CONFIG_ENTRY_URL_DOC, app.getInitConfidParameter(Constants.CONFIG_ENTRY_URL_DOC));
            internalConfig.put(Constants.CONFIG_ENTRY_URL_WEB_UPDATE, app.getInitConfidParameter(Constants.CONFIG_ENTRY_URL_WEB_UPDATE));
            internalConfig.put(Constants.CONFIG_ENTRY_URL_WEB_VER_FILE, app.getInitConfidParameter(Constants.CONFIG_ENTRY_URL_WEB_VER_FILE));
            internalConfig.put(Constants.CONFIG_ENTRY_EMAIL_SALES, app.getInitConfidParameter(Constants.CONFIG_ENTRY_EMAIL_SALES));
            internalConfig.put(Constants.CONFIG_ENTRY_EMAIL_SUPPORT, app.getInitConfidParameter(Constants.CONFIG_ENTRY_EMAIL_SUPPORT));
        }
        return internalConfig;
    }  
   
    /**
     * Get style resource from the autumo-ui base.
     * 
     * @return resource
     */
	public static String getBaseStyle() {
		return Constants.class.getResource(Constants.BASE_RESOURCE_PATH + Constants.STYLESHEET).toExternalForm();
	}
	
    /**
     * Get style resource from the autumo-ui base.
     * 
     * @return resource
     */
	public static URL getBaseHTMLStyleURL() {
		return Constants.class.getResource(Constants.BASE_RESOURCE_PATH + Constants.STYLESHEET_HTML);
	}

    /**
     * Get style resource from the autumo-ui base.
     * 
     * @return resource
     */
	public static String getBaseHTMLStyle() {
		return Constants.class.getResource(Constants.BASE_RESOURCE_PATH + Constants.STYLESHEET_HTML).toExternalForm();
	}
	
    /**
     * Get style resource from the autumo-ui base.
     * 
     * @return resource
     */
	public static URL getBaseStyleURL() {
		return Constants.class.getResource(Constants.BASE_RESOURCE_PATH + Constants.STYLESHEET);
	}
	
    /**
     * Get a resource from the autumo-ui base.
     * 
     * @param relativeResourcePath relative resource path
     * @return resource
     */
	public static String getBaseResource(String relativeResourcePath) {
		return Constants.class.getResource(Constants.BASE_RESOURCE_PATH + relativeResourcePath).toExternalForm();
	}
	
    /**
     * Get a resource from the autumo-ui base.
     * 
     * @param relativeResourcePath relative resource path
     * @return resource
     */
	public static URL getBaseResourceURL(String relativeResourcePath) {
		return Constants.class.getResource(Constants.BASE_RESOURCE_PATH + relativeResourcePath);
	}
	
    /**
     * Get a resource stream from the autumo-ui base.
     * 
     * @param relativeResourcePath relative resource path
     * @return resource stream
     */
	public static InputStream getBaseResourceStream(String relativeResourcePath) {
		return Constants.class.getResourceAsStream(Constants.BASE_RESOURCE_PATH + relativeResourcePath);
	}
	
    /**
     * Get a resource path in external form.
     * 
     * <p>The resource must be an application resource; if it may be missing because
     * the autumo-ui base ships a default, use {@link #getAppResourceOrBase(BaseApp, String)}
     * instead. A missing resource is reported here with a readable exception instead of
     * a {@code NullPointerException} deep inside the call chain.</p>
     * 
     * @param app the application
     * @param resourcePath relative or absolute resource path
     * @return resource path in external form
     */
	public static String getAppResource(BaseApp app, String resourcePath) {
		final URL resource = app.getClass().getResource(resourcePath);
		if (resource == null)
			throw new MissingResourceException(
					"Application resource '" + resourcePath + "' not found in '"
							+ app.getClass().getName() + "' - ship it in the application or use"
							+ " Helper.getAppResourceOrBase(...) if the autumo-ui default should be used!",
					app.getClass().getName(), resourcePath);
		return resource.toExternalForm();
	}

    /**
     * Get a resource path in external form: the application's resource if it has
     * one, otherwise the autumo-ui base resource of the same path.
     * 
     * <p>This is the lookup for framework panes whose content an application may,
     * but does not have to, provide: the default {@code html/remove_<lang>.html}
     * of the 'Remove App Data' pane lives in the autumo-ui base, and an
     * application ships its own file only if it wants different wording. Without
     * this fallback such a pane would fail with a {@code NullPointerException}
     * for every application that ships nothing.</p>
     * 
     * <p>A resource that neither the application nor the autumo-ui base ships is
     * reported with a readable {@link MissingResourceException} instead.</p>
     * 
     * @param app the application
     * @param resourcePath relative or absolute resource path
     * @return resource path in external form
     */
	public static String getAppResourceOrBase(BaseApp app, String resourcePath) {
		final URL resource = app.getClass().getResource(resourcePath);
		if (resource != null)
			return resource.toExternalForm();
		final URL base = Constants.class.getResource(Constants.BASE_RESOURCE_PATH + resourcePath);
		if (base == null)
			throw new MissingResourceException(
					"Resource '" + resourcePath + "' not found in the application '"
							+ app.getClass().getName() + "' nor in the autumo-ui base resources!",
					app.getClass().getName(), resourcePath);
		return base.toExternalForm();
	}

    /**
     * Get the style sheets of an application scene: the autumo-ui base style first and,
     * if the application ships one, its own application stylesheet
     * ({@code style/application.css}) on top.
     * 
     * <p>This is the combination the main window has — a base look, extended by the
     * application — and dialogs must use the same combination to match it: an application
     * stylesheet that only carries overrides (navigation tree, fonts, colours of its own)
     * still gets the framework's palette, and an application without any sheet gets the
     * framework look instead of an unstyled dialog.</p>
     * 
     * @param app the application
     * @return style sheet URLs, base style first, application style second or none
     */
	public static List<String> getApplicationStyleSheets(BaseApp app) {
		final List<String> sheets = new ArrayList<>();
		sheets.add(getBaseStyle());
		if (app.getClass().getResource(Constants.STYLESHEET) != null)
			sheets.add(getAppResource(app, Constants.STYLESHEET));
		return sheets;
	}

    /**
     * Icon sizes of an application icon, ascending: 16, 32, 48, 64, 128 and 256 pixels.
     */
	public static final int[] APPLICATION_ICON_SIZES = {16, 32, 48, 64, 128, 256};

    /**
     * Get the application icons of an application: the application's own icons if it
     * ships any, otherwise the default autumo-ui logo.
     * 
     * <p>An application defines its own icon by shipping any of:</p>
     * <ul>
     * <li>{@code icons/app-16.png} ... {@code icons/app-256.png} — any subset of
     * {@link #APPLICATION_ICON_SIZES}, the sharp icon for each window-manager size,</li>
     * <li>{@code icons/app.png} — a single icon (at least 256&times;256 pixels) that
     * is used for every size.</li>
     * </ul>
     * 
     * <p>Both are application resources, resolved relative to the application class.
     * If the application ships neither, the framework's default logo
     * ({@code icons/autumo-ui-logo-16.png} ... {@code icons/autumo-ui-logo-256.png})
     * is used, so every autumo-ui application has an application icon on all
     * operating systems without any further effort. The images are returned in
     * ascending size order — the last one is the largest and is what the macOS dock
     * icon of a packaged application is built from (jpackage {@code --icon}).</p>
     * 
     * @param app the application
     * @return application icon resource URLs in ascending size order, possibly empty
     */
	public static List<URL> getApplicationIcons(BaseApp app) {
		final List<URL> icons = new ArrayList<>();
		for (int size : APPLICATION_ICON_SIZES) {
			final URL icon = app.getClass().getResource(Constants.ICON_APP_PREFIX + size + ".png");
			if (icon != null)
				icons.add(icon);
		}
		if (!icons.isEmpty())
			return icons;
		final URL single = app.getClass().getResource(Constants.ICON_APP);
		if (single != null) {
			icons.add(single);
			return icons;
		}
		for (int size : APPLICATION_ICON_SIZES) {
			final URL icon = Constants.class.getResource(
					Constants.BASE_RESOURCE_PATH + Constants.ICON_APP_DEFAULT_PREFIX + size + ".png");
			if (icon != null)
				icons.add(icon);
		}
		return icons;
	}

    /**
     * Get a resource URL.
     * 
     * @param app the application
     * @param resourcePath relative or absolute resource path
     * @return resource path in external form
     */
	public static URL getAppResourceURL(BaseApp app, String resourcePath) {
		return app.getClass().getResource(resourcePath);
	}
	
    /**
     * Get a resource stream from the application.
     * 
     * @param app the application
     * @param resourcePath relative or absolute resource path
     * @return resource stream
     */
	public static InputStream getAppResourceStream(BaseApp app, String resourcePath) {
		return app.getClass().getResourceAsStream(resourcePath);
	}
	
    /**
     * Get a text translation.
     * 
     * @param app the application
     * @param translationId translation ID
     * @return translated text
     */
	public static String getTranslatedText(BaseApp app, String translationId) {
		return Helper.getMessageBundle(app.getBaseResourcePath()).getString(translationId);
	}

    /**
     * Get a text translation.
     * 
     * @param app the application
     * @param translationId translation ID
     * @param variables format variables
     * @return translated text
     */
	public static String getTranslatedText(BaseApp app, String translationId, Object ... variables) {
		String val = Helper.getMessageBundle(app.getBaseResourcePath()).getString(translationId);
		return MessageFormat.format(val, variables);
	}
	
    /**
     * Get the message bundle of the active language.
     * 
     * <p>Two gettext catalogues are merged, the application's one wins: its own
     * {@code lang/messages.<lang>.po} and the autumo-ui framework's
     * {@code /ch/autumo/ui/lang/messages.<lang>.po}. An application therefore only has to
     * translate its own texts; the framework's keys (its panes, dialogs and the installer)
     * always resolve, and the application may still override any framework text by defining
     * the key in its own catalogue. There are no {@code .properties} catalogues anymore.</p>
     * 
     * <p>The merged bundle is cached per base resource path and language; a language change
     * ({@link #setLanguage(String)}) clears the cache.</p>
     * 
	 * @param baseResourcePath base resource path, e.g. '/ch/autumo/.../',
	 * 			no backslashes! If {@code null} or empty, the autumo-ui
	 * 			base resource path is used
     * @return resource bundle
     */
    public static ResourceBundle getMessageBundle(String baseResourcePath) {
    	if (baseResourcePath == null || baseResourcePath.length() == 0)
    		baseResourcePath = Constants.BASE_RESOURCE_PATH;
    	if (!baseResourcePath.endsWith("/"))
    		baseResourcePath += "/";
    	final String lang = getLanguage();
    	final String cacheKey = baseResourcePath + "|" + lang;
    	final ResourceBundle cached = MESSAGE_BUNDLES.get(cacheKey);
    	if (cached != null)
    		return cached;
    	// First catalogue wins: the application's texts are looked up before the framework's.
    	final ResourceBundle bundle = new PoResourceBundle(lang, baseResourcePath,
    			Constants.BASE_RESOURCE_PATH);
    	MESSAGE_BUNDLES.put(cacheKey, bundle);
    	return bundle;
    }
    
    
    /**
     * Get the language the UI is currently displayed in. It is derived from
     * the 'user.language' system property on first use and can be changed at
     * runtime with {@link #setLanguage(String)}.
     * 
     * @return language code, e.g. 'en'; always one of {@link #getSupportedLanguages()}
     */
    public static String getLanguage() {
    	if (language == null) {
    		final String lang = System.getProperty("user.language");
    		language = isSupportedLang(lang) ? baseLanguage(lang) : Constants.LANG_EN;
    	}
    	return language;
    }
    
    /**
     * Get all languages supported by autumo-ui.
     * 
     * @return language codes, e.g. 'de', 'en'
     */
    public static String[] getSupportedLanguages() {
    	return Constants.LANGUAGES;
    }
    
    /**
     * Switch the UI language at runtime. This sets the JVM default locale and
     * the 'user.language' system property, drops the cached resource bundles
     * and notifies all registered {@link #addLanguageListener(Runnable) language
     * listeners}. A region is ignored ({@code de_CH} selects {@code de}); unsupported
     * or {@code null} values fall back to English.
     * 
     * <p>This covers the framework resources only (window titles, About/HTML
     * panes, dialogs). An application that translates its own texts has to
     * re-apply them in a language listener.</p>
     * 
     * @param lang language code, e.g. 'de'
     */
    public static void setLanguage(String lang) {
    	if (!isSupportedLang(lang))
    		lang = Constants.LANG_EN;
    	else
    		lang = baseLanguage(lang);
    	if (lang.equals(language))
    		return;
    	language = lang;
    	Locale.setDefault(Locale.forLanguageTag(lang));
    	System.setProperty("user.language", lang);
    	ResourceBundle.clearCache();
    	MESSAGE_BUNDLES.clear();
    	for (final Runnable listener : LANGUAGE_LISTENERS) {
    		try {
    			listener.run();
    		} catch (Exception e) {
    			LOG.error("Language listener failed!", e);
    		}
    	}
    }
    
    /**
     * Register a listener that is called after the language changed.
     * 
     * @param listener listener, e.g. one that re-applies the application texts
     */
    public static void addLanguageListener(Runnable listener) {
    	if (listener != null)
    		LANGUAGE_LISTENERS.add(listener);
    }
    
    /**
     * Remove a language listener again.
     * 
     * @param listener listener to remove
     */
    public static void removeLanguageListener(Runnable listener) {
    	LANGUAGE_LISTENERS.remove(listener);
    }
    
    
    /**
     * Is the language supported by the app?
     * 
     * <p>A region is ignored, i.e. 'de_CH' is supported because 'de' is (autumo-ui's
     * own {@link Constants#LOCALE_DE} is 'de_CH').</p>
     * 
     * @param lang lang code, e.g. 'en'
     * @return true if so
     */
    public static boolean isSupportedLang(String lang) {
    	final String code = baseLanguage(lang);
    	if (code == null)
    		return false;
    	for (int i = 0; i < Constants.LANGUAGES.length; i++) {
			if (Constants.LANGUAGES[i].equals(code))
				return true;
		}
    	return false;
    }
    
    /**
     * Reduce a language specification to its base language: trims, lower-cases,
     * unifies the separator and drops a region, e.g. 'DE_ch' -> 'de'.
     * 
     * @param lang language specification, may be {@code null}
     * @return base language code or {@code null} if the input is {@code null}/blank
     */
    private static String baseLanguage(String lang) {
    	if (lang == null)
    		return null;
    	final String code = lang.trim().toLowerCase(Locale.ROOT).replace('_', '-');
    	if (code.length() == 0)
    		return null;
    	final int region = code.indexOf('-');
    	return region > 0 ? code.substring(0, region) : code;
    }
    
    /**
     * File has an extension.
     * 
     * @param file file
     * @return true if there's an extension
     */
    public static boolean hasExtension(Path file) {
        return extensionDotIndex(file) != -1;
    }
    
    /**
     * Get extension dot index.
     * 
     * @param file file
     * @return extension dot index
     */
    public static int extensionDotIndex(Path file) {
        final String fileName = file.getFileName().toString();
        int lastDotIndex = fileName.lastIndexOf('.');
        if (
            lastDotIndex == -1 || lastDotIndex == 0
            || fileName.length() - lastDotIndex > MAX_FILE_EXTENSION_CHAR_COUNT
        ) {
            return -1;
        } else {
            return lastDotIndex;
        }
    }

    /**
     * Create a web view with a proper local storage for the
     * underlying web engine.
     * 
     * @param app application
     * @return web view
     */
    public static WebView createWebView(BaseApp app) {
    	final WebView webView = new WebView();
    	webView.getEngine().setUserDataDirectory(new File(OSUtils.getPropertiesPath(app.getInternalAppName())));
    	return webView;
    }

    /**
     * Get loaded country data.
     * 
     * @return country data
     */
    public static CountryData getCountryData() {
        return Constants.COUNTRY_DATA;
    }
    
    /**
     * File name without extension from path.
     * 
     * @param path path
     * @return name without extension from path
     */
    public static String nameFromPath(Path path) {
        final String fileName = path.getFileName().toString();
        int extensionDotIndex = extensionDotIndex(path);
        if (extensionDotIndex == -1) {
            return fileName;
        } else {
            return fileName.substring(0, extensionDotIndex);
        }
    }
    
    /**
     * Open an URL on the desktop.
     * 
     * @param url URL
     */
	public static void openURL(String url) {
		if (Desktop.isDesktopSupported()) {
			try {
				final URI uri = new URI(url);
				Desktop.getDesktop().browse(uri);
			} catch (IOException | URISyntaxException e) {
				LOG.error("Couldn't open url: " + url, e);
			}
		}
	}

    /**
     * Open a folder on the desktop.
     * 
     * @param folder folder
     * @return true, if folder could be opened
     */
	public static boolean openFolder(String folder) {
		if (folder == null || folder.length() == 0)
			return false;
		final File file = new File(folder);
		Desktop desktop = null;
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
			try {
				desktop.open(file);
				return true;
			} catch (Exception e) {
				LOG.error("Couldn't open folder: " + folder, e);
				return false;
			}
		}
		return false;
	}
     
    /**
     * Edit a file on the desktop.
     * 
     * @param file file
     */
	public static void editFile(String file) {
		final File f = new File(file);
		Desktop desktop = null;
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
			try {
				desktop.edit(f);
			} catch (IOException e) {
				LOG.error("Couldn't edit file: " + file, e);
			}
		}
	}

    /**
     * Open a file on the desktop.
     * 
     * @param file file
     */
	public static void openFile(String file) {
		final File f = new File(file);
		Desktop desktop = null;
		if (Desktop.isDesktopSupported()) {
			desktop = Desktop.getDesktop();
			try {
				desktop.open(f);
			} catch (IOException e) {
				LOG.error("Couldn't open file: " + file, e);
			}
		}
	}
    
	/**
	 * Choose a folder.
	 *  
	 * @param defaultDir default directory or null
	 * @param parent parent stage
	 * @param title title
	 * @param desc description
	 * @return absolute directory path
	 */
    public static String chooseFolder(String defaultDir, Stage parent, String title, String desc) {

    	DirectoryChooser d = new DirectoryChooser();
    	d.setTitle(title);
    	
        if (defaultDir != null && new File(defaultDir).exists())
        	d.setInitialDirectory(new File(defaultDir));

        File dir = d.showDialog(parent);
        if (dir == null)
        	return null;
        
        String ds = dir.getAbsolutePath();
        if (!ds.endsWith(OSUtils.FILE_SEPARATOR))
            ds += OSUtils.FILE_SEPARATOR;

        return ds;
    }
    
	/**
	 * Choose a file.
	 *  
	 * @param defaultDir default directory or null
	 * @param parent parent stage
	 * @param title title
	 * @param desc description
	 * @param extension allowed extension
	 * @return absolute file path
	 */
    public static String chooseFile(String defaultDir, Stage parent, String title, String desc, String extension) {

    	FileChooser f = new FileChooser();
    	f.setTitle(title);
    	
        if (defaultDir != null && new File(defaultDir).exists())
        	f.setInitialDirectory(new File(defaultDir));

        f.getExtensionFilters().addAll(new ExtensionFilter(desc, "*."+ extension));
        
        File file = f.showOpenDialog(parent);
        if (file == null)
        	return null;
        
        String ds = file.getAbsolutePath();
        if (!ds.endsWith(OSUtils.FILE_SEPARATOR))
            ds += OSUtils.FILE_SEPARATOR;

        return ds;
    }    
    
	/**
	 * Choose a file for saving.
	 *  
	 * @param defaultDir default directory or null
	 * @param parent parent stage
	 * @param title title
	 * @param desc description
	 * @param extension allowed extension
	 * @return absolute file path
	 */
    public static String saveFile(String defaultDir, Stage parent, String title, String desc, String extension) {

    	FileChooser f = new FileChooser();
    	f.setTitle(title);
    	
        if (defaultDir != null && new File(defaultDir).exists())
        	f.setInitialDirectory(new File(defaultDir));

        f.getExtensionFilters().addAll(new ExtensionFilter(desc, "*."+ extension));
        
        File file = f.showSaveDialog(parent);
        if (file == null)
        	return null;
        
        String ds = file.getAbsolutePath();
        if (!ds.endsWith(OSUtils.FILE_SEPARATOR))
            ds += OSUtils.FILE_SEPARATOR;

        return ds;
    }   
    
    /**
     * Return an Atari-busy-cursor!
     * 
     * @return Atari-busy-cursor
     */
    public static Cursor atariBusy() {
    	// Phosphor "circle-notch" glyph rendered to an image (the busy cursor needs an
    	// Image, not a node). Rendered off-scene with an explicit fill so it is visible.
    	final javafx.scene.text.Text glyph = ch.autumo.ui.fx.control.Icons.create("circle-notch", 32);
    	if (glyph != null) {
    		glyph.setStyle("-fx-fill: #dddddd;");
    		final javafx.scene.image.WritableImage image = glyph.snapshot(new javafx.scene.SnapshotParameters(), null);
    		if (image != null) {
    			return new ImageCursor(image);
    		}
    	}
    	// No PNG fallback any more: the cursor is a Phosphor glyph (or the default cursor).
    	return Cursor.DEFAULT;
    }
    
    
    /////// Special procedures
    
    private static long startTime = 0;
    /**
     * Start timer.
     */
    public static void startTime() {
        startTime = System.nanoTime();
    }
    /**
     * Stop timer.
     * @param id id foe info logging
     * @param clazz class for info clogging
     */
    public static void stopTime(String id, @SuppressWarnings("rawtypes") Class clazz) {
            final long time = System.nanoTime() - startTime;
            LOG.info(id + " - Time elapsed: " + TimeUnit.MILLISECONDS.convert(time, TimeUnit.NANOSECONDS) + "ms");
    }
    
    /**
     * Get a text translation with fallback.
     * 
     * @param app the application
     * @param translationId translation ID
     * @param fallback fallback text if not found
     * @return translated text
     */
    public static String getText(BaseApp app, String translationId, String fallback) {
        try {
            String val = Helper.getMessageBundle(app.getBaseResourcePath()).getString(translationId);
            return val == null || val.isEmpty() ? fallback : val;
        } catch (Exception e) {
            return fallback;
        }
    }
}
