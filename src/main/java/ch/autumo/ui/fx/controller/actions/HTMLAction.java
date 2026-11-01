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
package ch.autumo.ui.fx.controller.actions;

import java.io.File;

import ch.autumo.ui.utils.Helper;
import ch.autumo.ui.utils.UI.HyperLinkRedirectListener;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;

/**
 * HTML Action.
 */
public class HTMLAction extends BaseAction {

	private String template = null;
	private String cssFileRelPath = null;
	
	/**
	 * Default constructor.
	 */
	public HTMLAction() {
	}
	
	/**
	 * Constructor with template name and CSS file location.
	 * 
	 * @param template HTML template name ({template_name}_{lang}.html)
	 * @param cssFileRelPath path to CSS style-sheet or null
	 */	
	public HTMLAction(String template, String cssFileRelPath) {
		this.template = template;
		this.cssFileRelPath = cssFileRelPath;
		
	}
	
	@Override
	public void execute() {

    	final String lang = Helper.getLanguage();
		
		final BorderPane pane = controller.getBody();
		// Individual insets, depending on the element that is placed!
		BorderPane.setMargin(pane, new Insets(0, 6, 0, 6));
		
		final GridPane grid = new GridPane();
		pane.setStyle("-fx-border-color: #555555");
		pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		final WebView webView = new WebView();
		webView.getEngine().setUserDataDirectory(new File(Helper.getPropertiesPath(app)));
		webView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		webView.getEngine().getLoadWorker().stateProperty().addListener(new HyperLinkRedirectListener(webView));
		
		if (cssFileRelPath == null)
			webView.getEngine().setUserStyleSheetLocation(Helper.getBaseHTMLStyle());
		else
			webView.getEngine().setUserStyleSheetLocation(Helper.getAppResource(app, cssFileRelPath));
		
		webView.getEngine().load(Helper.getAppResource(app, "html/"+template+"_"+lang+".html"));
		
		GridPane.setVgrow(webView, Priority.ALWAYS);
		GridPane.setHgrow(webView, Priority.ALWAYS);
		grid.add(webView, 0, 0);
		pane.setCenter(grid);
	}
	
}
