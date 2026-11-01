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

import ch.autumo.ui.exception.ProcessException;

/**
 * Task listener for installation controller.
 */
public interface InstallEndListener {

	/**
	 * Will be called when the installer is finished.
	 * 
	 * @param ex an exception if any or null
	 */
    public void completed(ProcessException ex);
    
}
