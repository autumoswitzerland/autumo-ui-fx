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
import ch.autumo.ui.fx.controller.DefaultInfoController;
import ch.autumo.ui.fx.model.DefaultPaneModel;
import javafx.stage.Stage;

/**
 * Default Info Pane View.
 */
public class DefaultInfoView extends BaseBorderView {

    /**
     * Default Info Pane view.
     * 
     * @param app the application
     * @param stage stage
     */
    public DefaultInfoView(BaseApp app, Stage stage) {
    	this(app, stage, null);
    }
	
    /**
     * Default Info Pane view.
     * 
     * @param app the application
     * @param stage stage
     * @param styleResourcePath relative style ('style/style.css') file name or fully qualified resource path
     */
    public DefaultInfoView(BaseApp app, Stage stage, String styleResourcePath) {
    	super(app, stage, styleResourcePath);
    	final DefaultPaneModel model = new DefaultPaneModel(this);
    	this.setModel(model);
        this.setController(new DefaultInfoController(app, stage, model, this));
    }
	
}
