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
import java.util.ResourceBundle;

import ch.autumo.ui.fx.BaseApp;
import ch.autumo.ui.fx.control.HTMLLabel;
import ch.autumo.ui.fx.control.Icons;
import ch.autumo.ui.fx.model.DefaultPaneModel;
import ch.autumo.ui.utils.Helper;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Default Info Controller.
 */
public class DefaultInfoController extends BaseController implements Initializable {
    
    @FXML
    private Pane defauktInfoHeader;

    @FXML
    private Label defaultInfoHeaderLabel;

    @FXML
    private ImageView defaultInfoHeaderImage;
    
    @FXML
    private Label defauktInfoHeaderLabel;
    
    @FXML
    private BorderPane defaultInfoBody;
    
    @FXML
    private Pane defaultInfoBodyFrame;
    
    @FXML
    private BorderPane defaultInfoFooter;

    @FXML
    private HTMLLabel defaultInfoFooterLabel;

    @FXML
    private BorderPane defaultInfoFooterCenter;
    
    @FXML
    private Button defaultInfoFooterButton;
    
    @FXML
    private BorderPane defaultInfoFooterButtons;
    
    /**
     * Controller. 
     * 
     * @param app app
     * @param stage stage
     * @param model model
     * @param view view
     */
	public DefaultInfoController(BaseApp app, Stage stage, DefaultPaneModel model, Pane view) {
		super(app, stage, model, view);
	}
			
	@Override
	public void init(URL location, ResourceBundle resources, double toolBarShift) {
		defaultInfoBody.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
	}

	/**
	 * Customize the default info pane view.
	 * 
	 * @param title title
	 * @param iconFile icon file name (a resource in autumo-ui library) or absolute icon file name path to resource
	 * @param footerInfoText footer text or null
	 */
	public void customize(String title, String iconFile, String footerInfoText) {
		
		defaultInfoHeaderLabel.setText(title);
		
		if (Icons.isAvailable(iconFile)) {
			// Phosphor icon name (e.g. "info"): show the glyph directly in the header
			// (not a rendered bitmap), so it stays crisp, transparent and themeable.
			final Text glyph = Icons.create(iconFile, 40);
			if (glyph != null) {
				glyph.setStyle("-fx-fill: #dddddd;");
				// Wrap in a fixed-size StackPane so the glyph is centred both horizontally
				// and vertically in the 48x48 slot the header reserves for its icon.
				final javafx.scene.layout.StackPane box = new javafx.scene.layout.StackPane(glyph);
				box.setPrefSize(48, 48);
				box.setMinSize(48, 48);
				box.setMaxSize(48, 48);
				box.setAlignment(javafx.geometry.Pos.CENTER);
				final javafx.scene.layout.BorderPane header =
						(javafx.scene.layout.BorderPane) defaultInfoHeaderImage.getParent();
				if (header != null) {
					header.setRight(box);
					javafx.scene.layout.BorderPane.setMargin(box, new javafx.geometry.Insets(0, 8, 0, 0));
					javafx.scene.layout.BorderPane.setAlignment(box, javafx.geometry.Pos.CENTER_RIGHT);
				}
				defaultInfoHeaderImage.setVisible(false);
				defaultInfoHeaderImage.setManaged(false);
			}
		} else if (iconFile.startsWith("/"))
			defaultInfoHeaderImage.setImage(new Image(Helper.getAppResourceStream(app, iconFile)));
		else
			defaultInfoHeaderImage.setImage(new Image(Helper.getBaseResourceStream("icons/" + iconFile)));

		if (footerInfoText != null)
			defaultInfoFooterLabel.setText(footerInfoText);
	}

	/**
	 * Remove all buttons from the footer. Useful if you want to remove the
	 * default close button before adding customized buttons only.
	 * 
	 * @return amount of buttons removed
	 */
	public int removeAllFooterButtons() {
		final int size = defaultInfoFooterButtons.getChildren().size();
		defaultInfoFooterButtons.getChildren().clear();
		return size;
	}
	
	/**
	 * Close the dialog.
	 */
    @FXML
	public void close() {
    	getStage().close();
    }
	
    /**
     * Get the body (border pane).
     * @return body
     */
	public BorderPane getBody() {
		return defaultInfoBody;
	}
	
	/**
	 * Get the footer.
	 * @return footer
	 */
	public BorderPane getFooter() {
		return defaultInfoFooter;
	}
	
	/**
	 * Get the footer information label.
	 * @return footer information label
	 */
	public HTMLLabel getFooterLabel() {
		return defaultInfoFooterLabel;
	}

	/**
	 * Get the (usually) empty middle pane of the footer.
	 * @return middle footer pane
	 */
	public BorderPane getFooterCenter() {
		return defaultInfoFooterCenter;
	}

	/**
	 * Get the pane with all buttons.
	 * @return button pane
	 */
	public BorderPane getFooterButtons() {
		return defaultInfoFooterButtons;
	}
	
	/**
	 * Get the standard footer button, usually "Okay".
	 * @return standard footer button
	 */
	public Button getFooterButton() {
		return defaultInfoFooterButton;
	}
	
}
