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

import ch.autumo.ui.fx.BaseApp;
import ch.autumo.ui.fx.controller.DefaultInfoController;
import ch.autumo.ui.utils.UI;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Base Action for Controllers.
 */
public abstract class BaseAction implements Action {

	/** Reference to application. */
	protected BaseApp app = null;
	/** Reference to controller. */
	protected DefaultInfoController controller = null;
	
	/** Stage modality. */
	protected Modality modality = Modality.APPLICATION_MODAL;
	
	/**
	 * Base action. Window modality is set to
	 * {@link Modality#APPLICATION_MODAL}.
	 */
	public BaseAction() {
	}

	/**
	 * Base action with application explicitly set and therefore 
	 * not reset by any showPane-method in {@link UI}!
	 * 
	 * @param app application
	 */
	public BaseAction(BaseApp app) {
		this.app = app;
	}
	
	/**
	 * Base action with window modality and an application 
	 * explicitly set and therefore not reset by any showPane-
	 * method in {@link UI}!
	 * 
	 * See {@link Modality}.
	 * 
	 * @param app application
	 * @param modality window modality
	 */
	public BaseAction(BaseApp app, Modality modality) {
		this.app = app;
		this.modality = modality;
	}
	
	/**
	 * Base action with window modality;
	 * see {@link Modality}.
	 * 
	 * @param modality window modality
	 */
	public BaseAction(Modality modality) {
		this.modality = modality;
	}
	
	/**
	 * Get the application.
	 * @return application
	 */
	public BaseApp getApp() {
		return app;
	}

	/**
	 * Set the application.
	 * @param app application
	 */
	public void setApp(BaseApp app) {
		this.app = app;
	}

	/**
	 * Get the controller.
	 * @return controller
	 */
	public DefaultInfoController getController() {
		return controller;
	}

	/**
	 * Get the stage.
	 * @return stage
	 */
	public Stage getStage() {
		if (controller != null)
			return controller.getStage();
		return null;
	}

	/**
	 * Get the scene.
	 * @return scene
	 */
	public Scene getScene() {
		if (controller != null)
			return controller.getStage().getScene();
		return null;
	}

	/**
	 * Set controller.
	 * @param controller controller
	 */
	public void setController(DefaultInfoController controller) {
		this.controller = controller;
	}

	/**
	 * Set stage modality.
	 * @param modality stage modality
	 */
	public void setStageModality(Modality modality) {
		this.modality = modality;
	}
	
	/**
	 * Get stage modality.
	 */
	public Modality getStageModality() {
		return modality;
	}

	/**
	 * Run the action.
	 */
	public final void run() {
		Platform.runLater(new Runnable() {
		    @Override
		    public void run() {
		    	BaseAction.this.execute();
            }
        });
	};
	
	
	@Override
	public abstract void execute();
	
}
