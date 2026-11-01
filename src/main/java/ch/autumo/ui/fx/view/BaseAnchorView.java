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
package ch.autumo.ui.fx.view;

import ch.autumo.ui.fx.BaseApp;
import ch.autumo.ui.fx.controller.BaseController;
import ch.autumo.ui.fx.model.BaseModel;
import ch.autumo.ui.utils.Helper;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * FX Base Anchor View. 
 */
public class BaseAnchorView extends AnchorPane {

    private BaseApp app;
    private Stage stage;
    /** Model. */
    protected BaseModel model;
    /** Controller. */
    protected BaseController controller;
    
    /**
     * Base Anchor view.
     * 
     * <p>The scene carries the autumo-ui base style first; the application's own style sheet
     * follows on top if it ships one, i.e. if {@code styleResourcePath} is not {@code null} and
     * resolves to an application resource. A later style sheet wins per property, so an
     * application only overrides what it really wants - the base look stays the foundation.</p>
     * 
     * @param app app
     * @param stage stage
     * @param styleResourcePath resource path to a style-sheet file, {@code null} for the base look only
     */
    public BaseAnchorView(BaseApp app, Stage stage, String styleResourcePath) {
    	
        this.app = app;
        this.stage = stage;
        
        final Scene scene = new Scene(this);
        
        scene.getStylesheets().add(Helper.getBaseStyle());
        if (styleResourcePath != null)
        	scene.getStylesheets().add(Helper.getAppResource(app, styleResourcePath));
        
        stage.setScene(scene);
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
     * Set the model.
     * @param model model
     */
	public void setModel(BaseModel model) {
		this.model = model;
	}

	/**
	 * Get controller
	 * @return controller
	 */
	public BaseController getController() {
		return controller;
	}
	
	/**
	 * Set the controller.
	 * @param controller controller
	 */
	public void setController(BaseController controller) {
		this.controller = controller;
	}

    /**
     * Show view or not.
     * 
     * @param visible true to show this view.
     */
	public void show(boolean visible) {
		if (visible)
			this.stage.show();
		else
			this.stage.hide();
	}
	
}
