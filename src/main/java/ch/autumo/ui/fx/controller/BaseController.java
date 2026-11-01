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
package ch.autumo.ui.fx.controller;

import java.io.File;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import ch.autumo.ui.Constants;
import ch.autumo.ui.fx.BaseApp;
import ch.autumo.ui.fx.control.Icons;
import ch.autumo.ui.fx.model.BaseModel;
import ch.autumo.ui.utils.Helper;
import ch.autumo.ui.utils.OSUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * FX Base Controller.
 */
public abstract class BaseController implements Initializable {

	/** Application. */
    protected BaseApp app;
	/** Stage. */
    protected Stage stage;
	/** Model. */
    protected BaseModel model;
	/** View. */
    protected Pane view;

	/** The recent menu. */
    private Menu menuRecent;
	/** The clear all recent menu item. */
    private MenuItem menuItemClearAllRecent;
    
    private SeparatorMenuItem menuSepClearAllRecent;
    private boolean disableMacOSMenuDetach = false;

	/** The language menu. */
    private Menu menuLanguage;
	/** The language menu items, keyed by language code. */
    private final Map<String, RadioMenuItem> languageItems = new LinkedHashMap<>();

	/** Flag icons of the supported languages in the autumo-ui base resources. */
	private static final String LANG_ICON = "icons/lang_";
	/** Phosphor glyph used for the language sub-menu itself (no PNG fallback). */
	private static final String LANG_MENU_GLYPH = "translate";
    
    /**
     * Main controller. 
     * 
     * @param app app
     * @param stage stage
     * @param model model
     * @param view view
     */
	public BaseController(BaseApp app, Stage stage, BaseModel model, Pane view) {
		this.model = model;
		this.view = view;
		this.stage = stage;
		this.app = app;
	}
	
    /**
     * Get the application.
     * @return application
     */
    public BaseApp getApp() {
		return app;
	}
    
    /**
     * Get the stage.
     * @return stage
     */
    public Stage getStage() {
		return stage;
	}
    
	/**
	 * Get model.
	 * @return model
	 */
	public BaseModel getModel() {
		return model;
	}
    
	/**
	 * Get view.
	 * @return model
	 */
	public Pane getView() {
		return view;
	}	

	/**
	 * Override if you want to execute more actions on
	 * specific keyboard shortcuts for the stage/scene associated.
	 * 
	 * @param key key pressed
	 */
	public void keyPressed(KeyEvent key) {
	}

	/**
	 * Override if you want to execute actions on ESC key
	 * pressed.
	 * 
	 * @param key key pressed
	 */
	public void escKeyPressed(KeyEvent key) {
    	stage.close();
	}
		
	/**
	 * Initialize recent menu structure.
	 * 
	 * @param menuRecent the 'recent' sub-menu
	 * @param menuItemClearAllRecent the 'clear all entry' 
	 * 			menu entry or null (will be created) 
	 */
	public void initializeRecentMenu(Menu menuRecent, MenuItem menuItemClearAllRecent) {
		
		if (menuRecent == null)
			return;
		
		this.menuRecent = menuRecent;
		
		if (menuItemClearAllRecent == null)
			this.menuItemClearAllRecent = new MenuItem(Helper.getTranslatedText(app, "menu.Clear"));
		else
			this.menuItemClearAllRecent = menuItemClearAllRecent;
			
	    this.menuItemClearAllRecent.setOnAction(new EventHandler<ActionEvent>() {
	        public void handle(ActionEvent event) {
	        	app.clearRecentFiles();
	        	BaseController.this.menuRecent.getItems().clear();
	        }
	    });
	    this.menuSepClearAllRecent = new SeparatorMenuItem();		
	}

	/**
	 * Get the 'recent' sub-menu.
	 * 
	 * @return 'recent' sub-menu
	 */
	public Menu getMenuRecent() {
		return menuRecent;
	}
	
	/**
	 * Get the 'clear all entry' menu entry.
	 * 
	 * @return 'clear all entry' menu entry
	 */
	public MenuItem getMenuItemClearAllRecent() {
		return menuItemClearAllRecent;
	}

	/**
	 * Get the language sub-menu.
	 * 
	 * @return language sub-menu or null, if {@link #initializeLanguageMenu(Menu)} was not called
	 */
	public Menu getMenuLanguage() {
		return menuLanguage;
	}
	
	/**
	 * Initialize the language sub-menu: one radio item per language supported by
	 * autumo-ui, each with the language's flag and its name in that language
	 * ('English', 'Deutsch'). The item of the current language is selected, so an
	 * application does not have to care about the language it was started with.
	 * The sub-menu itself gets the framework's language icon, unless the
	 * application has already set its own graphic on it.
	 * 
	 * <p>Selecting an item switches the language of the whole application, see
	 * {@link #switchLanguage(String)}. Call this while the application builds its
	 * menu bar.</p>
	 * 
	 * @param menuLanguage language sub-menu, e.g. 'Help &gt; Language'
	 */
	public void initializeLanguageMenu(Menu menuLanguage) {
		if (menuLanguage == null)
			return;
		this.menuLanguage = menuLanguage;
		this.languageItems.clear();
		// the language sub-menu gets the framework's language icon, unless the
		// application has already put its own graphic on the menu
		if (menuLanguage.getGraphic() == null) {
			// Phosphor "translate" icon (icon font). No PNG fallback: lang_* / language-16.png
			// are no longer shipped as menu icons.
			final Node phosphor = Icons.create(LANG_MENU_GLYPH, 16);
			if (phosphor != null)
				menuLanguage.setGraphic(phosphor);
		}
		final ToggleGroup group = new ToggleGroup();
		for (final String lang : Helper.getSupportedLanguages()) {
			final Locale locale = Locale.forLanguageTag(lang);
			final RadioMenuItem item = new RadioMenuItem(locale.getDisplayLanguage(locale));
			item.setGraphic(new ImageView(new Image(
					Helper.getBaseResource(LANG_ICON + lang + ".png"), 0, 0, true, true)));
			item.setToggleGroup(group);
			item.setSelected(lang.equals(Helper.getLanguage()));
			item.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					switchLanguage(lang);
				}
			});
			menuLanguage.getItems().add(item);
			this.languageItems.put(lang, item);
		}
	}
	
	/**
	 * Marks the radio item of the given language as selected.
	 * 
	 * @param lang language code, e.g. 'de'
	 */
	public void selectLanguageItem(String lang) {
		final RadioMenuItem item = this.languageItems.get(lang);
		if (item != null)
			item.setSelected(true);
	}
	
	/**
	 * Is the radio item of the given language the selected one?
	 * 
	 * @param lang language code, e.g. 'de'
	 * @return true, if {@link #initializeLanguageMenu(Menu)} added the language and it is selected
	 */
	public boolean isLanguageSelected(String lang) {
		final RadioMenuItem item = this.languageItems.get(lang);
		return item != null && item.isSelected();
	}
	
	/**
	 * Switch the application language at runtime.
	 * 
	 * <p>The framework does its part here: the language of autumo-ui follows (framework
	 * bundles, framework dialogs, the window title and the About/license panes), the choice
	 * is stored as an application configuration parameter in the user's application
	 * configuration ('cfg/app.cfg', see {@link BaseApp#setConfigurationParameter(String, String)})
	 * so the next start comes up in it, and the language menu selection is moved.</p>
	 * 
	 * <p>Afterwards {@link #languageChanged()} lets the application re-apply the texts it
	 * sets itself.</p>
	 * 
	 * @param lang language code, e.g. 'de'
	 */
	public void switchLanguage(String lang) {
		if (lang == null || lang.equals(Helper.getLanguage()))
			return;
		Helper.setLanguage(lang);
		stage.setTitle(Helper.getText(app, "application.name", stage.getTitle()));
		app.setConfigurationParameter(Constants.PROP_KEY_LANGUAGE, lang);
		app.storeProps();
		this.selectLanguageItem(lang);
		this.languageChanged();
	}
	
	/**
	 * Called after the language was switched, so an application can re-apply the texts of
	 * its own controls; the framework texts, the window title, the language menu and the
	 * stored application parameter already follow.
	 * 
	 * <p>The default does nothing; an application with own texts overrides it.</p>
	 */
	protected void languageChanged() {
	}
	
	/**
	 * Rebuild recent file list.
	 */
    public void rebuildRecentList() {
    	    	
		if (this.menuRecent == null)
			return;
    	
    	Platform.runLater(new Runnable() {
			@Override
			public void run() {
		    	menuRecent.getItems().clear();
		        boolean first = true;
		        
		        final List<File> recentFiles = app.getRecentFiles();
		        final int size = recentFiles.size();
		        
		        for (final File file : recentFiles) {

		            final MenuItem mi = new MenuItem(file.getName());
		            mi.setOnAction(new EventHandler<ActionEvent>() {
		                public void handle(ActionEvent event) {
		                    innerOpen(file);
		                }
		            });
		            
		            if (first) {
		                mi.setAccelerator(new KeyCodeCombination(KeyCode.O, KeyCombination.SHIFT_DOWN, KeyCombination.META_DOWN));
		                first = false;
		            }
		            
		            menuRecent.getItems().add(mi);
		        }
		        if (size > 0) {
		        	menuRecent.getItems().add(menuSepClearAllRecent);
		        	menuRecent.getItems().add(menuItemClearAllRecent);
		        }
			}
    	});
    }

    /**
     * Prevent macOS menu detach.
     */
	public final synchronized void disableMacOSMenuDetach() {
		disableMacOSMenuDetach = true;
	}
    
	/**
	 * Only called on macOS.
	 * 
	 * Get menu bar; implement this, if you want to detach the application menu on
	 * macOS system and to add it to macOS its desktop menu. If you implement this,
	 * you might want to implement the method {@link #getToolBar()}!
	 * 
	 * Note: This is usually used for main views only!
	 * 
	 * If this method returns null, the menu bar stays attached to the application
	 * and {@link #getToolBar()} is not called.
	 * 
	 * Overwrite if necessary.
	 * 
	 * @return menu bar.
	 */
	public MenuBar getMenuBar() {
		return null;
	}
    
	/**
	 * Only called on macOS.
	 * 
	 * Returns the tool bar; see {@link #getMenuBar()}. It is only called if
	 * {@link #getMenuBar()} returns not null! It repositions the tool bar after
	 * detaching the menu bar. The tool bar should be inside a pane with a shifted
	 * top anchor ('AnchorPane.topAnchor'); it is set to zero!
	 * 
	 * @return tool bar pane.
	 */
	public Pane getToolBar() {
		return null;
	}

    /**
     * Whether it is safe to leave app: {@code false} when the user cancels the platform exit.
     *
     * @return {@code true} when the caller may proceed and discard the state
     */
    public boolean confirmDiscardChanges() {
    	return true;
    }
    
	/**
	 * Create a tool buttonm for the tool bar.
	 * 
     * @param iconName Phosphor glyph name
     * @param text text
     * @param tip tool-tip id any
     * @param action action to be executed id any
     * @return styled button.
	 */
    public Button toolButton(String iconName, String text, String tip, Runnable action) {
        final Text icon = Icons.create(iconName, 40);
        
        final javafx.scene.layout.StackPane iconBox = new javafx.scene.layout.StackPane();
        iconBox.setPrefSize(48, 48);
        iconBox.setMinSize(48, 48);
        iconBox.setMaxSize(48, 48);
        iconBox.setAlignment(javafx.geometry.Pos.CENTER);
        if (icon != null) {
            iconBox.getChildren().add(icon);
        }
        
        final Button b = new Button(text, iconBox);
        b.setContentDisplay(ContentDisplay.TOP);
        b.setGraphicTextGap(2);
        b.getStyleClass().add("toolbar-button");
        
        if (tip != null) {
            b.setTooltip(new Tooltip(tip));
        }

        if (action == null) {
            b.setDisable(true);
        } else {
            b.setOnAction(_ -> action.run());
        }
        
        return b;
    }
    
    /**
     * Create a tool separator for the tool bar.
     * 
     * @return styled separator.
     */
    public Separator toolSeparator() {
    	final Separator s = new Separator(Orientation.VERTICAL);
    	s.getStyleClass().add("toolbar-sep");
        return s;
    }
    
    /**
     * Create an image with specified iconf name found in app-resources or base-app-resoruces
     * icons-path; e.g. 'open-folder-48.png' -> 'ch/autumo/ui/icons/open-folder-48.png'.
     * 
     * @param iconFile icon file name
     * @return image view
     */
    public ImageView icon(String iconFile) {
        return new ImageView(new Image(
                Helper.getAppResourceOrBase(app, "icons/" + iconFile), 0, 0, true, true));
    }
    
    /**
     * Overwrite to open a file from recent list.
     * 
     * @param file file
     */
	protected void innerOpen(File file) {
	}	

	@Override
	public final void initialize(URL location, ResourceBundle resources) {
		
		double toolBarShift = 0.0;
		
		if (OSUtils.isMac() && !disableMacOSMenuDetach) {
			final MenuBar menuBar = this.getMenuBar();
			if (menuBar!= null && OSUtils.isMac())
				menuBar.useSystemMenuBarProperty().set(true);
			
			toolBarShift = 0.0;
			final Pane toolBar = this.getToolBar();
			if (toolBar != null) {
				toolBarShift = AnchorPane.getTopAnchor(toolBar);
				AnchorPane.setTopAnchor(toolBar, 0.0);
			}
		}
		
		// Initialization
		this.init(location, resources, toolBarShift);
	}

	/**
	 * Delegated initialization.
	 * 
	 * See {@link javafx.fxml.Initializable#initialize(URL, ResourceBundle)}.
	 * 
     * @param location
     * The location used to resolve relative paths for the root object, or
     * {@code null} if the location is not known.
     *
     * @param resources
     * The resources used to localize the root object, or {@code null} if
     * the root object was not localized.
     * 
     * @param toolBarShift if the tool bar has been shifted, this value is 
     * bigger than zero and can be used to adjust the UI layout. 
	 */
	public abstract void init(URL location, ResourceBundle resources, double toolBarShift);
	
}
