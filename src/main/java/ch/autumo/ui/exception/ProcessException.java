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
package ch.autumo.ui.exception;

/**
 * Exception for any wrong processings. 
 */
public class ProcessException extends Exception {

	private static final long serialVersionUID = 1L;
	
	/** Error code: Reading resource failed. */
	public static int ERR_CODE_RESOURCE_READ_FAILED = 1;
	/** Error code: Copying resource failed. */
    public static int ERR_CODE_RESOURCE_COPY_FAILED = 2;
	/** Error code: Writing a script/file output failed. */
    public static int ERR_CODE_WRITE_SCRIPT = 5;
	/** Error code: Writing to a log-file failed. */
    public static int ERR_CODE_WRITE_LOG_FILE = 8;
	/** Error code: Copying a project failed. */
    public static int ERR_CODE_COPY_PROJECT_FAILED = 9;
	/** Error code: General process error. */
    public static int ERR_CODE_PROCESS = 99;
    
    /**
     * Error code.
     */
    private int errCode = 0;

    /**
     * Process exception.
     * @param errCode error code
     */
    public ProcessException(int errCode) {
        super();
        this.errCode = errCode;
    }

    /**
     * Process exception.
     * @param errCode error code
     * @param message message
     */
    public ProcessException(int errCode, String message) {
        super(message);
        this.errCode = errCode;
    }

    /**
     * Get error code.
     * @return error code
     */
    public int getErrCode() {
        return errCode;
    }

    /**
     * Set error code.
     * @param errCode error code
     */
    public void setErrCode(int errCode) {
        this.errCode = errCode;
    }

}
