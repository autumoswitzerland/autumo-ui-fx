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

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import ch.autumo.ui.fx.BaseApp;
import ch.autumo.ui.fx.control.HTMLLabel;
import ch.autumo.ui.fx.model.DefaultPaneModel;
import ch.autumo.ui.fx.view.DefaultCodeEditorView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

/**
 * Default Code Editor Controller.
 *
 * <p>The pane chrome (header, body, footer) is defined by the FXML, exactly as for every other
 * pane; this controller only embeds the code editor built by the view into the body and offers
 * the setters for the additional footer buttons and the help/note text. Both are set before the
 * stage is shown, never in a constructor.</p>
 *
 * @see ch.autumo.ui.fx.view.DefaultCodeEditorView
 */
public class DefaultCodeEditorController extends DefaultInfoController {

	/** Container of the additional footer buttons, placed left of the standard footer button. */
	@FXML
	private HBox codeEditorButtons;

	/**
	 * Code editor controller.
	 *
	 * @param app app
	 * @param stage stage
	 * @param model model
	 * @param view view
	 */
	public DefaultCodeEditorController(BaseApp app, Stage stage, DefaultPaneModel model, DefaultCodeEditorView view) {
		super(app, stage, model, view);
	}

	@Override
	public void init(URL location, ResourceBundle resources, double toolBarShift) {
		// Lets the body grow with the window, as in every other pane.
		super.init(location, resources, toolBarShift);
		// The body comes from the FXML; embed the editor the view has built into it.
		// 'init' is called while the FXML is loaded, hence before the stage is shown.
		getBody().setCenter(((DefaultCodeEditorView) this.view).getEditorPane());
	}

	/**
	 * Set the additional footer buttons, e.g. 'Copy', 'Clear' and 'Format JSON'. They are placed
	 * left of the standard footer button ('Alright'), which is part of the FXML as in every other
	 * pane. Every button gets the standard footer button style class
	 * ({@code default-info-footer-button}) and its geometry, so all footer buttons look alike.
	 *
	 * <p>Set the buttons before the stage is shown.</p>
	 *
	 * @param buttons the buttons; {@code null} entries are skipped, an empty list or
	 *               {@code null} removes all additional buttons
	 */
	public void setButtons(List<Button> buttons) {
		this.codeEditorButtons.getChildren().clear();
		if (buttons == null)
			return;
		for (final Button button : buttons) {
			if (button == null)
				continue;
			// Standard footer button look, as defined by the FXML for 'Alright'.
			if (!button.getStyleClass().contains("default-info-footer-button"))
				button.getStyleClass().add("default-info-footer-button");
			button.setMinWidth(80.0);
			button.setPrefWidth(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
			button.setPrefHeight(25.0);
			this.codeEditorButtons.getChildren().add(button);
		}
	}

	/**
	 * Set the help/note text in the left footer slot (the same label the default info pane
	 * uses for its footer information).
	 *
	 * @param helpText the help text or {@code null} to clear the label
	 */
	public void setHelpText(String helpText) {
		getFooterLabel().setText(helpText == null ? "" : helpText);
	}

	/**
	 * Get the help/note label of the footer, e.g. for a transient note that fades out.
	 *
	 * @return help/note label
	 */
	public HTMLLabel getHelpLabel() {
		return getFooterLabel();
	}

}
