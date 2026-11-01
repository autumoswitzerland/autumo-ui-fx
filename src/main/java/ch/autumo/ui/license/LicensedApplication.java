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
package ch.autumo.ui.license;

import java.util.Properties;

/**
 * Licensed application - every licensed application must implement this.
 */
public interface LicensedApplication {

	/** License state. */
	public String LIC_STATE = "lic.state";

	/**
	 * License state: Initialize - Not registered and not invalidated.
	 */
	public String REG_STATE_INIT = "INIT";
	
	/**
	 * License state: Invalidated!
	 */
	public String REG_STATE_INV = "INV";
	
	/**
	 * License state: Checked &amp; valid.
	 */
	public String REG_STATE_CHCK = "CHCK";
	
	/** Registration information first name property key. */
	public String REG_ENTRY_FNAME = "registration.firstname";
	/** Registration information last name property key. */
	public String REG_ENTRY_LNAME = "registration.lastname";
	/** Registration information city property key. */
	public String REG_ENTRY_CITY = "registration.city";
	/** Registration information company property key. */
	public String REG_ENTRY_COMP = "registration.company";
	/** Registration information country property key. */
	public String REG_ENTRY_COUNTRY = "registration.country";
	/** Registration information email property key. */
	public String REG_ENTRY_EMAIL = "registration.email";

    /**
     * Get application title.
     * Part of the online license check, if applied. 
     * 
     * @return application title
     */
    public String getAppTitle();

    /**
	 * Get current application version as string "x.x.x".
	 * It is also used for online upgrade checks, if applied.
	 * 
	 * @return current application version
	 */
	public String getAppVersion();

	/**
	 * Every product has a unique key.
	 * Part of the online license check, if applied.
	 * 
	 * @return product key
	 */
	public String getProductKey();

	/**
	 * Get license properties.
	 * 
	 * @return license properties.
	 */
	public Properties getLicenseProperties();

	/**
	 * Get license key.
	 * 
	 * @return license key
	 */
	public String getLicenseKey();

	/**
	 * Validate license key.
	 * 
	 * @param firstName first name
	 * @param lastName last name
	 * @param city city
	 * @param company company
	 * @param country country
	 * @param email ameil
	 * @param licKey license key
	 * @return true if license key is valid
	 */
	public boolean validateLicenseKey(String firstName, String lastName, String city, String company, String country, String email, String licKey);
	
}
