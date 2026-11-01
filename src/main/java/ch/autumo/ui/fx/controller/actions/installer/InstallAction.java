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

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.autumo.ui.fx.BaseApp;
import ch.autumo.ui.fx.controller.actions.BaseAction;
import ch.autumo.ui.utils.ConfigurationManager;
import ch.autumo.ui.utils.Helper;
import ch.autumo.ui.utils.OSUtils;
import ch.autumo.ui.utils.UI;

/**
 * The install action. It has no controller and doesn't control a view, 
 * but instead it creates the necessary steps for all installation views.
 */
public class InstallAction extends BaseAction implements InstallationController {

	private final static Logger LOG = LoggerFactory.getLogger(InstallAction.class.getName());
	
	private final List<Step> steps = new ArrayList<Step>();
    
    private BaseStep currStep = null;
    
    private List<String> licStepOverOS = new ArrayList<String>();
    

    /**
     * Install action.
     * 
     * @param app application
     */
	public InstallAction(BaseApp app) {
		super(app);
		String s = ConfigurationManager.getInstance().getStringNoWarn("installer.licStepOverOS");
		if (s != null) {
			s = s.trim().toLowerCase().replace(" ", "");
			final String os[] = s.split(",");
			licStepOverOS.addAll(Arrays.asList(os));
		}
	}

	@Override
	public void execute() {
		this.refillSteps();
		this.start();
	}

	
	// Installation controller
	
	@Override
	public void start() {
        if (steps.size() > 0) {
            currStep = (BaseStep) steps.get(0);
            if (this.overTake()) {
            	this.next();
            	return;
            }
            currStep.initialize();
            UI.showPane(app, currStep, currStep.getTitle());
            this.setStageTitle(currStep);
        } else {
            synchronized (app) {
            	app.completed(null);
            }
        }
    }
	
	@Override
	public void next() {
        currStep.steppedOver();
		currStep.close();
        int currStepId = currStep.getStep();
        if (currStepId <= steps.size() ) {
            currStep = (BaseStep) steps.get(currStepId);
            if (this.overTake()) {
            	this.next();
            	return;
            }
            currStep.initialize();
            UI.showPane(app, currStep, currStep.getTitle());
            this.setStageTitle(currStep);
        }
    }

	@Override
	public void back() {
		currStep.close();
        int currStepId = currStep.getStep();
        if (currStepId - 1 > 0 ) {
            currStep = (BaseStep) steps.get(currStepId - 2);
            if (this.overTake()) {
            	this.back();
            	return;
            }
            currStep.initialize();
            UI.showPane(app, currStep, currStep.getTitle());
            this.setStageTitle(currStep);
        }		
	}

	@Override
	public void finish() {
		currStep.close();
        synchronized (app) {
        	app.completed(null);
        }
	}
	
	private void setStageTitle(BaseStep currStep2) {
		currStep.getController().getStage().setTitle(
				Helper.getTranslatedText(app, "application.name") + 
				" " + app.getAppVersion() + 
				" - " 
				+ Helper.getTranslatedText(app, "Installer.installation")
			);
	}
	
	private boolean overTake() {
        if (currStep instanceof StepLicense) {
        	if (licStepOverOS.contains("mac") && OSUtils.isMac())
        		return true;
        	else if (licStepOverOS.contains("win") && OSUtils.isWindows())
        		return true;
        	else if (licStepOverOS.contains("unix") && OSUtils.isUnix())
        		return true;
        	else
        		return false;
        } else {
        	return false;
        }
	}
	
    private void refillSteps() {

    	steps.clear();
    	BaseStep last = null;
    	
        final Properties p = new Properties();
        try {
        	
        	final InputStream is = Helper.getAppResourceStream(app, "cfg/installer-steps.cfg");
        	if (is == null)
        		return;
        				
			p.load(is);

	        for (int i = 1; i <= p.keySet().size(); i++) {
				final String stepClass = p.getProperty(""+i);
				if (stepClass != null) {
					final Class<?> clz = Class.forName(stepClass);
					final BaseStep step = (BaseStep) clz.getDeclaredConstructor().newInstance();
					step.initialize(i, this, app);
			        steps.add(step);
			        last = step;
				} else {
					break;
				}
			}
	        
	        // Mark the last step as the last!
	        if (last != null)
	        	last.last();

		} catch (Exception e) {
			LOG.error("Couldn't load installer steps from 'cfg/installer-steps.cfg'!", e);
			throw new RuntimeException("Couldn't load installer steps from 'cfg/installer-steps.cfg'!", e);
		}
        
        //steps.add(new Step1License(app, this));
        //steps.add(new Step2Dummy(app, this));
        //steps.add(new Step3Dummy(app, this));
    }	
	
}
