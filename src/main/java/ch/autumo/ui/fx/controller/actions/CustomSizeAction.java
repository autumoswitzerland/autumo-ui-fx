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

import javafx.stage.Modality;

/**
 * Custom Windows-Size Action.
 */
public abstract class CustomSizeAction extends BaseAction {

	private int width = 800;
	private int height = 600;

	/**
	 * Custom size action.
	 */
	public CustomSizeAction() {
	}
	
	/**
	 * Custom size action.
	 * 
	 * @param width width
	 * @param height height
	 */
	public CustomSizeAction(int width, int height) {
		this.width = width;
		this.height = height;
	}

	/**
	 * Custom size action.
	 * 
	 * @param width width
	 * @param height height
	 * @param modality window modality
	 */
	public CustomSizeAction(int width, int height, Modality modality) {
		super(modality);
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Set width.
	 * 
	 * @param width width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * Set height.
	 * 
	 * @param height height
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * Get width.
	 * 
	 * @return width
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Get height.
	 * 
	 * @return height
	 */
	public int getHeight() {
		return height;
	}

}
