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
package ch.autumo.ui.fx.controller.actions.installer;

import ch.autumo.ui.utils.Helper;
import ch.autumo.ui.utils.UI.HyperLinkRedirectListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;


/**
 * License Accept Installer Step.
 */
public class StepLicense extends BaseStep {
	
	/**
	 * Default constructor.
	 */
	public StepLicense() {
	}
	
	@Override
	public void execute() {
		
		final BorderPane pane = controller.getBody();
		// Individual insets, depending on the element that is placed!
		BorderPane.setMargin(pane, new Insets(0, 6, 0, 6));
		
		final GridPane grid = new GridPane();
		pane.setStyle("-fx-border-color: #555555");
		pane.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		
		final WebView webView = Helper.createWebView(app);
		webView.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		webView.getEngine().getLoadWorker().stateProperty().addListener(new HyperLinkRedirectListener(webView));
		webView.getEngine().setUserStyleSheetLocation(getHTMLStyleSheetLocation());
		webView.getEngine().load(Helper.getAppResource(app, "html/license_en.html"));
		
		GridPane.setVgrow(webView, Priority.ALWAYS);
		GridPane.setHgrow(webView, Priority.ALWAYS);
		grid.add(webView, 0, 0);
		pane.setCenter(grid);

		final BorderPane buttons = controller.getFooterButtons();
		final Button back = new Button(Helper.getTranslatedText(super.getApp(), "Installer.back"));
		BorderPane.setMargin(back, new Insets(0, 8, 0, 0));
		back.setPrefWidth(100);
		back.setMinWidth(100);
		back.getStyleClass().add("default-info-footer-button");
		buttons.setCenter(back);
		back.setOnAction(_ -> {
			super.getInstallationController().back();
    	});	
		
		Button next = controller.getFooterButton();
		next.setText(Helper.getTranslatedText(super.getApp(), "Installer.next"));
		next.setOnAction(_ -> {
			super.getInstallationController().next();
    	});
		next.setDisable(true);
		
		// replace empty label
		BorderPane footer = controller.getFooter();
		CheckBox accepted = new CheckBox(Helper.getTranslatedText(super.getApp(), "Installer.lic.accept"));
		accepted.getStyleClass().add("license-check-box");
		accepted.setPadding(new Insets(20, 0, 0, 8));
		accepted.setSelected(false);
		accepted.setOnAction(_ -> {
			if (accepted.isSelected())
				next.setDisable(false);
			else
				next.setDisable(true);				
    	});
		
		footer.setLeft(accepted);
	}

	@Override
	public void steppedOver() {
		super.getApp().licenseAccepted();
	}

	@Override
	public String getTitle() {
		return Helper.getTranslatedText(super.getApp(), "Installer.lic.title");
	}

	/**
	 * Returns the location of the HTML style sheet.
	 * Override to provide a custom style sheet.
	 *
	 * @return the CSS location
	 */
	public String getHTMLStyleSheetLocation() {
		return Helper.getBaseHTMLStyle();
	}

}
