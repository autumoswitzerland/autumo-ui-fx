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
package ch.autumo.ui.data;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.autumo.ui.Constants;

/**
 * Country data.
 */
public class CountryData {

	/** Logger. */
	private final static Logger LOG = LoggerFactory.getLogger(CountryData.class.getName());

	/** Country index for Switzerland (default). */
    public static int COUNTRY_INDEX_SWITZERLAND = 207;
	
    private static final String VALUE_SEPARATOR = ";";

    private final List<Country> countries = new ArrayList<Country>();
    
    /**
     * Country data.
     */
    public CountryData() {
        try (InputStreamReader isr = new InputStreamReader(CountryData.class.getResourceAsStream(Constants.FILE_COUNTRIES))) {
            String line;
            final LineNumberReader r = new LineNumberReader(isr);
            while ((line = r.readLine()) != null) {
                final String record[] = line.split(VALUE_SEPARATOR);
                countries.add(
                		new Country(
                				Integer.valueOf(record[0]).intValue(), 
                				String.valueOf(record[1]), 
                				record[2]
	                		)
	                	);
            }
        } catch (IOException ex) {
            LOG.error("Couldn't load country data!", ex);
        }
    }

    /**
     * Get loaded countries.
     * 
     * @return loaded countries
     */
    public List<Country> getCountries() {
        return countries;
    }

    /**
     * Country.
     */
    public static final class Country {
    	private int index = -1;
    	private String name = null;
    	private String imagePath = null;
    	/**
    	 * Country.
    	 * @param index index
    	 * @param name name
    	 * @param imagePath path to image icon
    	 */
    	public Country(int index, String name, String imagePath) {
    		this.index = index;
    		this.name = name;
    		this.imagePath = imagePath;
    	}
		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			return name.equals(((Country)obj).name);
		}
		@Override
		public String toString() {
			return "Country["+name+"]";
		}
		/**
		 * Get index.
		 * @return index
		 */
		public int getIndex() {
			return index;
		}
		/**
		 * Set index.
		 * @param index index
		 */
		public void setIndex(int index) {
			this.index = index;
		}
		/**
		 * Get name.
		 * @return name
		 */
		public String getName() {
			return name;
		}
		/**
		 * Set name.
		 * @param name name
		 */
		public void setName(String name) {
			this.name = name;
		}
		/**
		 * Get image path.
		 * @return image path
		 */
		public String getImagePath() {
			return imagePath;
		}
		/**
		 * Set image path.
		 * @param imagePath image path
		 */
		public void setImagePath(String imagePath) {
			this.imagePath = imagePath;
		}
    }
    
}
