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

import ch.autumo.ui.data.CountryData;
import ch.autumo.ui.fx.BaseApp;
import ch.autumo.ui.fx.model.DefaultPaneModel;
import ch.autumo.ui.fx.view.DefaultInfoView;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 * Registration Controller.
 */
public class RegistrationController extends DefaultInfoController {

	/** Field: First name. */
	@FXML
	protected TextField regFirstName;
	/** Field: Lst name. */
	@FXML
	protected TextField regLastName;
	/** Field: Company. */
	@FXML
	protected TextField regCompany;
	/** Field: City. */
	@FXML
	protected TextField regCity;
	/** Field: Country. */
	@FXML
	protected ComboBox<CountryData.Country> regCountry;
	/** Field: Email. */
	@FXML
	protected TextField regEmail;
	/** Area: License key. */
	@FXML
	protected TextArea regLicenseKey;
	/** Area: Problems. */
	@FXML
	protected TextArea regProblems;
	
	/** License pane. */
	@FXML
	protected BorderPane regPaneLicense;
	
	/**
	 * Registration controller.
	 * @param app application
	 * @param stage stage
	 * @param model model
	 * @param view view
	 */
	public RegistrationController(BaseApp app, Stage stage, DefaultPaneModel model, DefaultInfoView view) {
		super(app, stage, model, view);
	}

	@Override
	public void init(URL location, ResourceBundle resources, double toolBarShift) {
		super.init(location, resources, toolBarShift);
	}

	/**
	 * Get first name field.
	 * @return first name field
	 */
	public TextField getRegFirstName() {
		return regFirstName;
	}

	/**
	 * Get last name field.
	 * @return last name field
	 */
	public TextField getRegLastName() {
		return regLastName;
	}

	/**
	 * Get company field.
	 * @return company field
	 */
	public TextField getRegCompany() {
		return regCompany;
	}

	/**
	 * Get city field.
	 * @return city field
	 */
	public TextField getRegCity() {
		return regCity;
	}

	/**
	 * Get country entry.
	 * @return country entry
	 */
	public ComboBox<CountryData.Country> getRegCountry() {
		return regCountry;
	}

	/**
	 * Get first name field.
	 * @return first name field
	 */
	public TextField getRegEmail() {
		return regEmail;
	}

	/**
	 * License key.
	 * @return license key
	 */
	public TextArea getRegLicenseKey() {
		return regLicenseKey;
	}

	/**
	 * Get problems.
	 * @return problems
	 */
	public TextArea getRegProblems() {
		return regProblems;
	}

	/**
	 * Remove problems from view.
	 */
	public void removeRegProblems() {
		regPaneLicense.setBottom(null);
	}
	
}
