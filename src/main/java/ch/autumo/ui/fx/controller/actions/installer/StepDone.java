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
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;

/**
 * Done Installer Step.
 */
public class StepDone extends BaseStep {

	/**
	 * Default constructor.
	 */
	public StepDone() {
	}
	
	@Override
	public void execute() {
		
		final BorderPane pane = controller.getBody();
		// Individual insets, depending on the element that is placed!
		BorderPane.setMargin(pane, new Insets(0, 10, 0, 10));
		
		final Label info = new Label(Helper.getTranslatedText(super.getApp(), "Installer.done.info"));
		info.setFont(new Font(14));
		pane.setLeft(info);
		
		final Button finish = controller.getFooterButton();
		finish.setText(Helper.getTranslatedText(super.getApp(), "Installer.finish"));
		finish.setOnAction(_ -> {
			ic.finish();
    	});
		
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
	}

	@Override
	public String getTitle() {
		return Helper.getTranslatedText(super.getApp(), "Installer.done.title");
	}
	
}
