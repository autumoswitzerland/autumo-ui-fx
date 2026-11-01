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
package ch.autumo.ui.fx.control;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Encryption mode constants for the encoder.
 */
public final class EncoderModes {

    /** Default list of encryption modes offered by the encoder (none / Base64-SHA3 / ...). */
    public static final ObservableList<String> DEFAULT = FXCollections.observableArrayList(
            "0: none",
            "1: Base64 - SHA3-256 AES",
            "2: Base64 - SHA-256 AES",
            "3: Base64 - PBE MD5 DES",
            "4: Blowfish"
    );

    private EncoderModes() {
    }

    /**
     * Returns the default encoding mode list.
     * 
     * @return observable list of encoding mode strings
     */
    public static ObservableList<String> getDefault() {
        return DEFAULT;
    }
}
