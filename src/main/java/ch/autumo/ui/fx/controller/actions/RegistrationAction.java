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

import java.text.MessageFormat;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.autumo.ui.Constants;
import ch.autumo.ui.data.CountryData;
import ch.autumo.ui.data.CountryData.Country;
import ch.autumo.ui.fx.controller.RegistrationController;
import ch.autumo.ui.license.LicensedApplication;
import ch.autumo.ui.utils.Helper;
import ch.autumo.ui.utils.UI;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import net.synedra.validatorfx.Check.Context;
import net.synedra.validatorfx.Validator;


/**
 * Registration action.
 */
public class RegistrationAction extends BaseAction {

	/** Logger. */
	protected final static Logger LOG = LoggerFactory.getLogger(RegistrationAction.class.getName());
	
	private static final int MAX_LINE_LENGTH = 90;

	private LicensedApplication licApp = null;
	
	/**
	 * Constructor.
	 * 
	 * @param licApp licensed app
	 */
	public RegistrationAction(LicensedApplication licApp) {
		this.licApp = licApp;
	}
	
    /**
     * Email REGEX pattern
     */
    public static final Pattern REGEX_PATTERN_EMAIL = Pattern.compile("[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?");
	
	private Validator validator = new Validator();
	private StringBinding problemsText;
	
	private String licState = null;
    private boolean registered = false;

    
	@Override
	public void execute() {
		
        final RegistrationController regController = (RegistrationController) controller;
        regController.getRegProblems().setStyle("-fx-background-color: -fx-control-inner-background;");
        
        final List<CountryData.Country> countries = Constants.COUNTRY_DATA.getCountries();
        final ComboBox<Country> cBox = regController.getRegCountry();
        cBox.setButtonCell(new CountryComboBoxListCell());
        cBox.setCellFactory(
    		new Callback<ListView<CountryData.Country>, ListCell<CountryData.Country>>() {
        			@Override
        			public ListCell<CountryData.Country> call(ListView<CountryData.Country> p) {
        				return new CountryComboBoxListCell();
        			}
        		});
        cBox.getItems().addAll(countries);
        cBox.getSelectionModel().select(CountryData.COUNTRY_INDEX_SWITZERLAND); // Switzerland
        cBox.setStyle("-fx-background-color: #838383; -fx-text-fill: #000000;");

        this.installTextFormatter(regController);
        //regController.getRegLicenseKey().setTextFormatter(this.createTextFormatter());

        
		final Properties p = licApp.getLicenseProperties();
        if (!p.isEmpty()) {
        	regController.getRegFirstName().setText(p.getProperty(LicensedApplication.REG_ENTRY_FNAME));
        	regController.getRegLastName().setText(p.getProperty(LicensedApplication.REG_ENTRY_LNAME));
        	regController.getRegCity().setText(p.getProperty(LicensedApplication.REG_ENTRY_CITY));
        	regController.getRegCompany().setText(p.getProperty(LicensedApplication.REG_ENTRY_COMP));
        	regController.getRegEmail().setText(p.getProperty(LicensedApplication.REG_ENTRY_EMAIL));
            final String regCountry = p.getProperty(LicensedApplication.REG_ENTRY_COUNTRY);
        	regController.getRegCountry().getSelectionModel().select(new Country(-1, regCountry, null));
        }
        
        
        licState = p.getProperty(LicensedApplication.LIC_STATE);
        if (licState == null) {
            this.installValidation();
        	registered = false;
        } else if (licState.equals(LicensedApplication.REG_STATE_INIT)) {
            this.installValidation();
        	registered = false;
        } else if (licState.equals(LicensedApplication.REG_STATE_INV)) {
            this.installValidation();
        	registered = false;
        } else if (licState.equals(LicensedApplication.REG_STATE_CHCK)) {
        	// No validation necessary!
        	registered = true;
        }

        
        if (registered) {
        	// - Remove buy button if any -> Not installed.
        	// - Don't touch the default button
        	super.getController().getFooterLabel().setText(
        			MessageFormat.format(
        					Helper.getTranslatedText(app, "Registration.active"),
        					Helper.getTranslatedText(app, "application.name")
        				)
        			);
        	regController.getRegLicenseKey().setText(licApp.getLicenseKey());
        	regController.removeRegProblems();
        	this.disableFields();
        } else {
        	// NOTICE: Add/Enable buy button if any
        	super.getController().getFooterButton().setText(Helper.getTranslatedText(app, "Registration.okay"));
        	super.getController().getFooterButton().setOnAction(_ -> {
        		mainButtonPressed();
        	});
    		final BorderPane buttons = controller.getFooterButtons();
    		// - Add cancel button
    		final Button cancel = new Button(Helper.getTranslatedText(app, "Registration.cancel"));
    		BorderPane.setMargin(cancel, new Insets(0, 8, 0, 0));
    		cancel.setPrefWidth(100); cancel.setMinWidth(100);
    		cancel.getStyleClass().add("default-info-footer-button");
    		buttons.setCenter(cancel);
    		cancel.setOnAction(_ -> {
    			super.getStage().hide();
        	});	        	
        	super.getController().getFooterLabel().setText(
        			MessageFormat.format(
        					Helper.getTranslatedText(app, "Registration.notactive"),
        					Helper.getTranslatedText(app, "application.name")
        				)
        			);
        }
	}

	/** Workaround */
	private boolean ignoreOnce = false;
	private synchronized void installTextFormatter(RegistrationController regController) {
    	regController.getRegLicenseKey().textProperty().addListener((_, _, newValue) -> {
    		if (!ignoreOnce) {
				int ct = 0;
				int shift = 0;
				String nt = "";
				final String t = newValue;
				final int l = newValue.length();
				LINES: while (true) {
					for (int i = (MAX_LINE_LENGTH * shift); i < MAX_LINE_LENGTH + (MAX_LINE_LENGTH * shift); i++) {
						if (ct == l)
							break LINES;
						nt += t.charAt(i);
						ct++;
					}
					if (ct < l) {
						nt += "\n";
						shift++;
					} else
						break LINES;
				}
				ignoreOnce = !ignoreOnce;
				regController.getRegLicenseKey().setText(nt); // this triggers this method again, so ignore once
    		} else {
    			ignoreOnce = !ignoreOnce;
    		}
					
    	});
    }

	private void installValidation() {
		
        final RegistrationController regController = (RegistrationController) controller;
		
        this.setupProblemOutput(regController);
        
        validator.createCheck()
        	.withMethod(this::required)
        	.dependsOn("text", regController.getRegFirstName().textProperty())
        	.decorates(regController.getRegFirstName())
        	.immediate();

        validator.createCheck()
	    	.withMethod(this::required)
	    	.dependsOn("text", regController.getRegLastName().textProperty())
	    	.decorates(regController.getRegLastName())
	    	.immediate();

        validator.createCheck()
	    	.withMethod(this::required)
	    	.dependsOn("text", regController.getRegCity().textProperty())
	    	.decorates(regController.getRegCity())
	    	.immediate();
        
        validator.createCheck()
	    	.withMethod(this::required)
	    	.withMethod(this::matchEmail)
	    	.dependsOn("text", regController.getRegEmail().textProperty())
	    	.decorates(regController.getRegEmail())
	    	.immediate();
		
        validator.createCheck()
	    	.withMethod(this::required)
	    	.withMethod(this::validateRegistrationData)
	    	.dependsOn("text", regController.getRegLicenseKey().textProperty())
	    	.decorates(regController.getRegLicenseKey())
	    	.immediate();	
	}

	private void mainButtonPressed() {
		// We have to re-validate here!
		if (validator.validate()) {
			if (!registered) {
				boolean licensed = storeRegistrationData();
				if (licensed) {
					registered = true;
					final BorderPane buttons = controller.getFooterButtons();
					buttons.setCenter(null);
					final String text = MessageFormat.format(
							Helper.getTranslatedText(app, "Registration.active"),
							Helper.getTranslatedText(app, "application.name")
						);
					final RegistrationController regController = (RegistrationController) controller;
					regController.getFooterLabel().setText(text);
					regController.removeRegProblems();
					disableFields();
					UI.showFineInformationDialog(app, text);
				} else {
					UI.showFineInformationDialog(app, Helper.getTranslatedText(app, "Registration.stored"));
					getStage().hide();
				}
			} else {
				getStage().hide();
			}
		} else {
			// Nada, UI is update by the failed validation!
		}
	}
	
	private TextArea setupProblemOutput(RegistrationController regController) {
		final TextArea problems = regController.getRegProblems();
		problems.setEditable(false);
		problems.setPrefHeight(80);
		problems.setBackground(Background.EMPTY);
		problems.setFocusTraversable(false);
		problemsText = Bindings.createStringBinding(this::getProblemText, validator.validationResultProperty());
		problems.textProperty().bind(problemsText);
		return problems;
	}
	
	private String getProblemText() {
		return validator.validationResultProperty().get().getMessages().stream()
			.map(msg -> msg.getSeverity().toString() + ": " + msg.getText())
			.collect(Collectors.joining("\n"));
	}
	
	private void required(Context context) {
		String text = context.get("text");
		if (text == null || text.trim().isEmpty()) {
			context.error(Helper.getTranslatedText(app, "Registration.validation.requiered"));
		}
	}

	private void matchEmail(Context context) {
		String text = context.get("text");
		Matcher matcher = REGEX_PATTERN_EMAIL.matcher(text.trim());
        if (!matcher.matches()) {
			context.error(Helper.getTranslatedText(app, "Registration.validation.email"));
		}
	}

	/**
	 * Store rehistration data - overwrite.
	 * 
	 * @return true if registration has been successful 
	 */
    protected boolean storeRegistrationData() {
        return false;
    }
    
    private void validateRegistrationData(Context context) {
        final RegistrationController regController = (RegistrationController) controller;
        final ComboBox<Country> cBox = regController.getRegCountry();
        final Country country = cBox.getSelectionModel().getSelectedItem();
        //context.get("text");
        String licKey =  regController.getRegLicenseKey().getText().trim();
        final boolean result = licApp.validateLicenseKey(
        		regController.getRegFirstName().getText().trim(),
        		regController.getRegLastName().getText().trim(),
        		regController.getRegCity().getText().trim(),
        		regController.getRegCompany().getText().trim(),
        		country.getName(), 
        		regController.getRegEmail().getText().trim(),
        		licKey
        	);
        if (!result) {
			context.error(Helper.getTranslatedText(app, "Registration.validation.license.invalid"));
        }
    }
    
    private void disableFields() {
        final RegistrationController regController = (RegistrationController) controller;
    	regController.getRegFirstName().setEditable(false);
    	regController.getRegLastName().setEditable(false);
    	regController.getRegCity().setEditable(false);
    	regController.getRegCompany().setEditable(false);
    	regController.getRegEmail().setEditable(false);
        //phoneNumber.setEditable(false);
    	regController.getRegCountry().setEditable(false);
    	regController.getRegCountry().setDisable(true);
    	regController.getRegLicenseKey().setEditable(false);
    }
    
    /**
     * Country List Cell for Combo-Box. 
     */
	final class CountryComboBoxListCell extends ListCell<CountryData.Country> {
		@Override
		public void updateItem(Country item, boolean empty) {
			super.updateItem(item, empty);
            if (!empty && item != null) {
                if (item.getImagePath() != null) {
	                final Image image = new Image(RegistrationAction.class.getResource(item.getImagePath()).toExternalForm());
	                setGraphic(new ImageView(image));
                }
                setText(item.getName());
            } else {
                setText(null);
            }			
		}
    }
	 
}
