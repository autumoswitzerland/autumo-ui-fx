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
package ch.autumo.ui;

import java.text.SimpleDateFormat;
import java.util.Locale;

import ch.autumo.ui.data.CountryData;


/**
 * Constants.
 */
public interface Constants {

	/**
	 * System property key for manufacturer.
	 */
    public static final String SYS_KEY_SYSTEM_MANUFACTURER = "SYSTEM_MANUFACTURER";
	
	/**
	 * Relative application configuration path.
	 */
    public static final String CFG_PATH = "cfg/";
    
	/**
	 * The default relative path to the application configuration.
	 */
    public static final String FILE_INTERNAL_CONF = CFG_PATH + "app.cfg";
	
	/**
	 * Console log pattern.
	 */
	public static final String PROP_KEY_LOG_CONSOLE_PATTERN = "log.consolePattern";

	/**
	 * Console log pattern.
	 */
	public static final String PROP_KEY_LOG_FILE_PATTERN = "log.filePattern";
	
    /**
     * Language configuration/property key.
     */
    public static final String PROP_KEY_LANGUAGE = "language";
    
    /**
     * Recent item configuration/property key prefix.
     */
    public static final String PROP_KEY_RECENT = "recent.item";
    
    /**
     * License accepted configuration/property key. 
     */
    public static final String PROP_KEY_LIC_ACC = "licenseAccepted";

    /**
     * base resource path for 'autumo-ui' library.
     */
    public static final String BASE_RESOURCE_PATH = "/ch/autumo/ui/";
    
    /**
     * Country data file.
     */
    public static final String FILE_COUNTRIES = "/ch/autumo/ui/data/countries.csv";

    /**
     * Country data.
     */
    public static final CountryData COUNTRY_DATA = new CountryData();
    
    /**
     * Max. recent files.
     */
    public static final int RECENT_MAX_SIZE = 10;

    /**
     * Date format for license accepted time-stamp.
     */
    public static final SimpleDateFormat DATE_FORMAT_LIC_EXT = new SimpleDateFormat("yyyy-MM-dd HH-mm");

	/**
	 * Application style-sheet.
	 */
	public static final String STYLESHEET = "style/application.css";

	/**
	 * Application style-sheet with full resource path.
	 */
	public static final String STYLESHEET_FULL_PATH = "/ch/autumo/ui/style/application.css";

	/**
	 * Application HTML style-sheet.
	 */
	public static final String STYLESHEET_HTML = "style/html.css";

	/**
	 * Application HTML style-sheet with full resource path.
	 */
	public static final String STYLESHEET_HTML_FULL_PATH = "/ch/autumo/ui/style/html.css";

	/**
	 * Single application icon an application ships to define its own application
	 * icon (window and task-bar icon on all operating systems). An application
	 * that ships nothing gets the default autumo-ui logo.
	 * 
	 * @see ch.autumo.ui.utils.Helper#getApplicationIcons(ch.autumo.ui.fx.BaseApp)
	 */
	public static final String ICON_APP = "icons/app.png";

	/**
	 * Prefix of the size-specific application icon resources an application may
	 * ship instead of the single {@link #ICON_APP} icon: {@code icons/app-16.png}
	 * ... {@code icons/app-256.png}.
	 * 
	 * @see ch.autumo.ui.utils.Helper#getApplicationIcons(ch.autumo.ui.fx.BaseApp)
	 */
	public static final String ICON_APP_PREFIX = "icons/app-";

	/**
	 * Prefix of the default application icon shipped with 'autumo-ui':
	 * {@code icons/autumo-ui-logo-16.png} ... {@code icons/autumo-ui-logo-256.png}.
	 * 
	 * @see ch.autumo.ui.utils.Helper#getApplicationIcons(ch.autumo.ui.fx.BaseApp)
	 */
	public static final String ICON_APP_DEFAULT_PREFIX = "icons/autumo-ui-logo-";
	
    /**
     * Default Info view FXML.
     */
	public static final String VIEW_DEFAULT_INFO = "defaultInfoPane.fxml";

    /**
     * Registration view FXML.
     */
	public static final String VIEW_REGISTRATION = "registrationPane.fxml";

    /**
     * Default code editor view FXML.
     */
	public static final String VIEW_CODE_EDITOR = "codeEditorPane.fxml";
	
	/**
	 * Language 'de'.
	 */
    public static final String LANG_DE = "de";
    
	/**
	 * Language 'en'.
	 */
    public static final String LANG_EN = "en";
    
    /**
     * Locale 'de_CH'.
     */
    public static final Locale LOCALE_DE = Locale.of("de", "CH");
    
    /**
     * Locale 'en_US'.
     */
    public static final Locale LOCALE_EN = Locale.of("en", "US");

	/**
	 * Supported languages.
	 */
	public static String LANGUAGES[] = new String[] {LANG_DE, LANG_EN};

	// Property/configuration keys for different settings
	/** Configuration property key for purchase URL. */
    public static final String CONFIG_ENTRY_URL_PURCHASE = "url.purchase";
	/** Configuration property key for web-site URL. */
    public static final String CONFIG_ENTRY_URL_WEB = "url.website";
	/** Configuration property key for documentation URL. */
    public static final String CONFIG_ENTRY_URL_DOC = "url.documentation";
	/** Configuration property key for update URL. */
    public static final String CONFIG_ENTRY_URL_WEB_UPDATE = "url.website.updates";
	/** Configuration property key for version XML file URL. */
    public static final String CONFIG_ENTRY_URL_WEB_VER_FILE = "url.website.version.file";
	/** Configuration property key for sales email. */
    public static final String CONFIG_ENTRY_EMAIL_SALES = "email.sales";
	/** Configuration property key for support email. */
    public static final String CONFIG_ENTRY_EMAIL_SUPPORT = "email.support";
    
	// Property/configuration default values if no others can be determiend
	/** Default configuration property key for web-site URL. */
    public static final String DEF_CONFIG_ENTRY_URL_PURCHASE = "https://autumo.ch";
	/** Default configuration property key for web-site URL. */
    public static final String DEF_CONFIG_ENTRY_URL_WEB = "https://autumo.ch";
	/** Default configuration property key for documentation URL. */
    public static final String DEF_CONFIG_ENTRY_URL_DOC = "https://products.autumo.ch";
	/** Default configuration property key for update URL. */
    public static final String DEF_CONFIG_ENTRY_URL_WEB_UPDATE = "https://products.autumo.ch";
	/** Default configuration property key for version XML file URL. */
    public static final String DEF_CONFIG_ENTRY_URL_WEB_VER_FILE = "https://products.autumo.ch/xml/template.xml";
	/** Default configuration property key for sales email. */
    public static final String DEF_CONFIG_ENTRY_EMAIL_SALES = "info@autumo.ch";
	/** Default configuration property key for support email. */
    public static final String DEF_CONFIG_ENTRY_EMAIL_SUPPORT = "info@autumo.ch";
	
}
