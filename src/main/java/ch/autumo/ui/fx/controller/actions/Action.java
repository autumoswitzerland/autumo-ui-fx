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
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Action interface for panes.
 */
public interface Action {

	/**
	 * Get the application
	 * 
	 * @return application
	 */
	public BaseApp getApp();

	/**
	 * Set the application.
	 * 
	 * @param app application
	 */
	public void setApp(BaseApp app);
	
	/**
	 * Get controller.
	 * 
	 * @return controller
	 */
	public DefaultInfoController getController();

	/**
	 * Set controller.
	 * 
	 * @param controller controller
	 */
	public void setController(DefaultInfoController controller);

	/**
	 * Get stage.
	 * 
	 * @return stage
	 */
	public Stage getStage();
	
	/**
	 * Get scene.
	 * 
	 * @return scene
	 */
	public Scene getScene();
	
	/**
	 * Get window modality.
	 * 
	 * @return window modality
	 */
	public Modality getStageModality();
	
	/**
	 * Inner execution.
	 */
	public void run();
	
	/**
	 * Execute the action.
	 */
	public void execute();
	
}
