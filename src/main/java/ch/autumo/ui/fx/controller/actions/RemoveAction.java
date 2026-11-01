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

import java.util.Optional;

import ch.autumo.ui.utils.Helper;
import ch.autumo.ui.utils.UI;
import ch.autumo.ui.utils.UI.HyperLinkRedirectListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;

/**
 * Remove App Action.
 */
public class RemoveAction extends BaseAction {

	private String cssFileRelPath = null;

	/**
	 * Default constructor.
	 */
	public RemoveAction() {
	}
	
	/**
	 * Constructor with CSS file location.
	 * 
	 * @param cssFileRelPath path to CSS style-sheet or null
	 */
	public RemoveAction(String cssFileRelPath) {
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

		final WebView webView = Helper.createWebView(app);
		webView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		webView.getEngine().getLoadWorker().stateProperty().addListener(new HyperLinkRedirectListener(webView));
		
		if (cssFileRelPath == null)
			webView.getEngine().setUserStyleSheetLocation(Helper.getBaseHTMLStyle());
		else
			webView.getEngine().setUserStyleSheetLocation(Helper.getAppResource(app, cssFileRelPath));

		// The application may ship its own html/remove_<lang>.html; if it does not,
		// the framework's default text is used, so the pane works out of the box.
		webView.getEngine().load(Helper.getAppResourceOrBase(app, "html/remove_"+lang+".html"));
		
		GridPane.setVgrow(webView, Priority.ALWAYS);
		GridPane.setHgrow(webView, Priority.ALWAYS);
		
		// Change default clsoe button to cancel.
		final Button cancel = controller.getFooterButton();
		cancel.setText(Helper.getTranslatedText(super.getApp(), "button.Cancel"));
		
		final BorderPane buttons = controller.getFooterButtons();
		
		final Button remove = new Button(Helper.getTranslatedText(app, "button.Remove"));
		remove.getStyleClass().add("default-info-remove-button");
		BorderPane.setMargin(remove, new Insets(0, 8, 0, 0));
		remove.setPrefWidth(100);
		remove.setMinWidth(100);
		buttons.setCenter(remove);

		grid.add(webView, 0, 0);
		pane.setCenter(grid);		
		
		remove.setOnAction(_ -> {
			
			final Optional<ButtonType> result = UI.showFineWarningConfirmationDialog(super.getApp(), Helper.getTranslatedText(app, "dialog.Remove.ask"));
			if (result.isPresent() && result.get() == ButtonType.OK){
				
				super.getApp().removeConfig();

				UI.showFineInformationDialog(super.getApp(), Helper.getTranslatedText(app, "dialog.Remove.info"));
			    controller.getStage().hide();
			    // Hard exit, no new data must be stored!
			    System.exit(0);
			    
			} else {
			    controller.getStage().hide();
			}
    	});		
	}

}
