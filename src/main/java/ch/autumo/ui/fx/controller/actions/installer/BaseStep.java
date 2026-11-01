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

import ch.autumo.ui.fx.BaseApp;
import ch.autumo.ui.fx.controller.actions.BaseAction;

/**
 * Base step for installer.
 */
public abstract class BaseStep extends BaseAction implements Step {

	/** Current step in installer. */
	protected int stepId = -1;
	/** Reference to the installation controller. */
	protected InstallationController ic = null;
	
	private boolean last = false;
	
	
	/**
	 * Default constructor.
	 */
	protected BaseStep() {
	}
	
	@Override
	public final void initialize(int stepId, InstallationController ic, BaseApp app) {
		this.stepId = stepId;
		this.ic = ic;
		this.app = app;
	}
	
	/**
	 * Get installation controller.
	 * 
	 * @return installation controller
	 */
	public final InstallationController getInstallationController() {
		return ic;
	}

	@Override
    public final int getStep() {
		return this.stepId;
	}
	
    @Override
    public final boolean isFirst() {
    	return this.stepId == 1;
    }

    @Override
    public final boolean isLast() {
    	return last;
    }
    
    @Override
    public void initialize() {
    }
    
    @Override
    public void steppedOver() {
    }

    @Override
    public abstract String getTitle();
    
    @Override
    public int compareTo(Step o) {
        return Integer.valueOf(this.getStep()).compareTo(Integer.valueOf(o.getStep()));
    }

    /**
     * Mark as last.
     */
    public final void last() {
    	this.last = true;
    }
    
    /**
     * Close this step's stage.
     */
    public final void close() {
    	if (super.getController() != null) 
    		// possible if the step wasn't initialized (e.g. overtaken by installer)
    		super.getController().getStage().hide();
    }
    
}
