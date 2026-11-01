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
package ch.autumo.ui.utils;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.SystemUtils;


/**
 * Operating system utilities (paths, environment, process execution, ...).
 */
public class OSUtils {
    
	/**
	 * OS.
	 */
	private static String OS = System.getProperty("os.name").toLowerCase();

	/**
	 * Get Windows APPDATA directory.
	 */
    public static final String WIN_APPDATA_FOLDER = System.getenv("APPDATA" /*"LOCALAPPDATA"*/);
	
	/**
	 * User home directory.
	 */
    public static final String USER_HOME = System.getProperty("user.home");
	
    /**
     * System-specific separator.
     */
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    /**
     * System-specific file separator.
     */
    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    /**
     * System-specific temporary directory.
     */
    public static final String DIR_TEMP = System.getProperty("java.io.tmpdir");

    /**
     * System manufacturer.
     */
    private static String manufacturer = null;
	
	
	/**
	 * Default constructor.
	 */
	private OSUtils() {
	}
	
	/**
	 * Is Windows?
	 * 
	 * @return true if so
	 */
	public static boolean isWindows() {
		return SystemUtils.IS_OS_WINDOWS;
	}

	/**
	 * Is Mac?
	 * 
	 * @return true if so
	 */
	public static boolean isMac() {
		return SystemUtils.IS_OS_MAC;
	}

	/**
	 * Is Unix?
	 * 
	 * @return true if so
	 */
	public static boolean isUnix() {
		return SystemUtils.IS_OS_UNIX;
	}

	/**
	 * Is Solaris?
	 * 
	 * @return true if so
	 */
	public static boolean isSolaris() {
		return SystemUtils.IS_OS_SOLARIS;
	}
	
	/**
	 * Get operating system.
	 * 
	 * @return true if so
	 */
	public static String getOS() {
		return OS;	
	}
    
	/**
	 * Get user name.
	 * 
	 * @return user name
	 */
    public static String getUser() {
    	return System.getProperty("user.name");
    }

    /**
     * Get manufacturer for this application/system.
     * 
     * @return manufacturer name
     */
    public static String getManufacturer() {
		if (manufacturer == null)
			manufacturer = System.getProperty("SYSTEM_MANUFACTURER", "autumo");
		return manufacturer;
	}
    
    /**
     * Get root properties path.
     * 
     * @return root properties path
     */
    public static String getPropertiesRootPath() {
        if (isMac())
            return USER_HOME + FILE_SEPARATOR + "Library" + FILE_SEPARATOR + "Application Support" + FILE_SEPARATOR + getManufacturer() + FILE_SEPARATOR;
        if (isWindows())
            return WIN_APPDATA_FOLDER + FILE_SEPARATOR + getManufacturer() + FILE_SEPARATOR;
        if (isUnix())
            return USER_HOME + FILE_SEPARATOR;
        return USER_HOME + FILE_SEPARATOR;
    } 
    
    /**
     * Get properties path.
     * 
     * @param appName app name
     * @return properties path
     */
    public static String getPropertiesPath(String appName) {
        if (isMac())
            return USER_HOME + FILE_SEPARATOR + "Library" + FILE_SEPARATOR + "Application Support" + FILE_SEPARATOR + getManufacturer() + FILE_SEPARATOR + appName + FILE_SEPARATOR;
        if (isWindows())
            return WIN_APPDATA_FOLDER + FILE_SEPARATOR + getManufacturer() + FILE_SEPARATOR + appName + FILE_SEPARATOR;
        if (isUnix())
            return USER_HOME + FILE_SEPARATOR + "." + appName + FILE_SEPARATOR;
        return USER_HOME + FILE_SEPARATOR;
    }
    
    /**
     * Get default project root path.
     * 
     * @param projectFolder project folder
     * @return default project root path
     */
    public static String getDefaultProjectRootPath(String projectFolder) {
        if (isMac())
            return USER_HOME + FILE_SEPARATOR + projectFolder + FILE_SEPARATOR;
        if (isWindows())
            return USER_HOME + FILE_SEPARATOR + projectFolder + FILE_SEPARATOR;
        if (isUnix())
            return USER_HOME + FILE_SEPARATOR + projectFolder + FILE_SEPARATOR;
        return USER_HOME + FILE_SEPARATOR;
    }
    
	/**
	 * Get readable duration.
	 * 
	 * @param durationInMilliseconds duration in ms
	 * @param printUpTo The maximum timeunit that should be printed
	 * @return readable duration
	 */
	public static String getReadableDuration(long durationInMilliseconds, TimeUnit printUpTo) {
		
		long dy = TimeUnit.MILLISECONDS.toDays(durationInMilliseconds);
		long allHours = TimeUnit.MILLISECONDS.toHours(durationInMilliseconds);
		long allMinutes = TimeUnit.MILLISECONDS.toMinutes(durationInMilliseconds);
		long allSeconds = TimeUnit.MILLISECONDS.toSeconds(durationInMilliseconds);
		long allMilliSeconds = TimeUnit.MILLISECONDS.toMillis(durationInMilliseconds);
		
		final long hr = allHours - TimeUnit.DAYS.toHours(dy);
		final long min = allMinutes - TimeUnit.HOURS.toMinutes(allHours);
		final long sec = allSeconds - TimeUnit.MINUTES.toSeconds(allMinutes);
		final long ms = allMilliSeconds - TimeUnit.SECONDS.toMillis(allSeconds);
		
		switch (printUpTo) {
			case DAYS: return String.format("%d Days %d Hours %d Minutes %d Seconds %d Milliseconds", dy, hr, min, sec, ms);
			case HOURS: return String.format("%d Hours %d Minutes %d Seconds %d Milliseconds", hr, min, sec, ms);
			case MINUTES: return String.format("%d Minutes %d Seconds %d Milliseconds", min, sec, ms);
			case SECONDS: return String.format("%d Seconds %d Milliseconds", sec, ms);
			case MILLISECONDS: return String.format("%d Milliseconds", ms);
			default: return String.format("%d Days %d Hours %d Minutes %d Seconds %d Milliseconds", dy, hr, min, sec, ms);
		}
	}
	
	/**
	 * Find an executable on the environment path variable and return
	 * the full path or throw an exception.
	 *  
	 * @param name executable name, with or without extension
	 * @return full path of executable
	 * @throws IOException if executable cannot be found in PATH on OS
	 */
	public static String findExecutableOnPath(String name) throws IOException {
		if (isWindows()) {
			if (name.indexOf(".") == -1) {
				name = name.trim() + ".exe";
			}
		}
		final String envPaths = System.getenv("PATH");
	    for (String dirname : envPaths.split(File.pathSeparator)) {
	        File file = new File(dirname, name);
	        if (file.isFile() && file.canExecute()) {
	            return file.getAbsolutePath();
	        }
	    }
	    throw new IOException("Should have found the executable '"+name+"'!");
	}
	
	/**
	 * Get MAC address.
	 * @return mac address
	 */
	public static String getMacAddress() {
    	InetAddress ip;
		final StringBuilder sb = new StringBuilder();
    	try {
    		ip = InetAddress.getLocalHost();
    		final NetworkInterface network = NetworkInterface.getByInetAddress(ip);
    		byte[] mac = network.getHardwareAddress();
    		for (int i = 0; i < mac.length; i++) {
    			sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));		
    		}
    	} catch (Exception e){
    		return "00-00-00-00-00-00";
		}
    	return sb.toString();
	}
	
	/**
	 * Get runtime parameter map
	 * @return RT parameter map
	 */
	public static Map<String, String> getRuntimeParameters() {
		
		final Map<String, String> rtParams = new HashMap<String, String>();
		
		final RuntimeMXBean bean = ManagementFactory.getRuntimeMXBean();
		final List<String> list = bean.getInputArguments();
		for (Iterator<String> iterator = list.iterator(); iterator.hasNext();) {
			String assign = iterator.next();
			String kv[] = assign.split("=");
			if (kv.length > 1 && kv[1] != null && kv[1].length() > 0)
				rtParams.put(kv[0], kv[1]);
		}		
		return rtParams;
	}

	/**
	 * Get random number.
	 *  
	 * @param min min
	 * @param max max
	 * @return random number
	 */
	public static int randomInt(int min, int max) {
		if (min >= max) {
			throw new IllegalArgumentException("max must be greater than min");
		}
		final Random r = new Random();
		return r.nextInt((max - min) + 1) + min;		
	}
	
	/**
	 * Get random string.
	 * 
	 * @param size size
	 * @return random string
	 */
	public static String randomString(int size ) { 
        // chose a Character random from this String 
        final String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                                    + "0123456789"
                                    + "abcdefghijklmnopqrstuvxyz"; 
  
        // create StringBuffer size of AlphaNumericString 
        final StringBuilder sb = new StringBuilder(size); 
        for (int i = 0; i < size; i++) { 
            // generate a random number between 
            // 0 to AlphaNumericString variable length 
            int index = (int)(AlphaNumericString.length() * Math.random()); 
            // add Character one by one in end of sb 
            sb.append(AlphaNumericString .charAt(index)); 
        } 
        return sb.toString(); 
    } 

    /**
     * Get lowered file name extension without dot.
     * 
     * @param fileName file name
     * @return lowered file name extension
     */
    public static String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf('.') + 1, fileName.length()).toLowerCase();
    }
    
	/**
	 * Get temporary directory.
	 * 
	 * @return temporary directory
	 */
	public static String getTemporaryDirectory() {
		String dir = System.getProperty("java.io.tmpdir");
		if (!dir.endsWith(FILE_SEPARATOR))
			dir += FILE_SEPARATOR;
		return dir;
	}
	
	/**
	 * Delete files.
	 * 
	 * @param files file list
	 */
    public static void deleteFiles(File files[]) {
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory())
                deleteFiles(files[i].listFiles());
            files[i].delete();
        }
    }

    /**
     * Mark files for deletion on exit.
     * 
     * @param files file list
     */
    public static void markForDeletion(File files[]) {
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory())
            	markForDeletion(files[i].listFiles());
            files[i].deleteOnExit();
        }
    }
    
	/**
	 * Get major java version, without the old scheme for older version
	 * than Java version 9; Numbers from 1-20 (+).
	 *  
	 * @return java major version
	 */
	public static int getMajorJavaVersion() {
	    String version = System.getProperty("java.version");
	    if(version.startsWith("1.")) {
	        version = version.substring(2, 3);
	    } else {
	        int dot = version.indexOf(".");
	        if(dot != -1) { version = version.substring(0, dot); }
	    } return Integer.parseInt(version);
	}
	
    /**
     * Exit console program, due to unprocessable error.
     */
	public static void fatalExit() {
		exit(1);
	}
    /**
     * Exit console program, due to an error.
     */
	public static void errorExit() {
		exit(1);
	}
    /**
     * Exit because of invalid argument use.
     */
	public static void invalidArgumentsExit() {
		exit(128);
	}

    /**
     * Exit console program, due desired end.
     */
	public static void normalExit() {
		exit(0);
	}
    /**
     * Exit console program.
     * @param code exit code
     */
	public static void exit(int code) {
		System.exit(code);
	}
	
}
