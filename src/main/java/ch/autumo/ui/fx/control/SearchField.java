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
package ch.autumo.ui.fx.control;

import ch.autumo.ui.fx.listener.PasteCallback;
import javafx.scene.control.TextField;

/**
 * Search field.
 */
public class SearchField extends TextField {

	private PasteCallback callback = null;
	
	/**
	 * Create a search field.
	 */
	public SearchField() {
		super();
	}
	
	/**
	 * Create a search field.
	 * 
	 * @param text initial text
	 */
	public SearchField(String text) {
		super(text);
	}
			
	/**
	 * Create a search field with a paste callback.
	 * 
	 * @param callback paste callback
	 */
	public SearchField(PasteCallback callback) {
		this.callback = callback;
	}
	
	@Override
    public void paste() {
		this.callback.pasted();
		super.paste();
    }

	/**
	 * Get paste callback.
	 * 
	 * @return paste callback
	 */
	public PasteCallback getCallback() {
		return callback;
	}

	/**
	 * Set paste callback.
	 * 
	 * @param callback paste callback
	 */
	public void setCallback(PasteCallback callback) {
		this.callback = callback;
	}
	
}
