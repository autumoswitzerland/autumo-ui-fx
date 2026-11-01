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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javafx.scene.text.Font;

/**
 * Central registry for fonts loaded by the application.
 * Register each loaded font file so that concrete style names
 * (regular, bold, italic, bold-italic) can be resolved by family.
 */
public final class FontRegistry {

    private static Font defaultFont = Font.getDefault();
    private static final Map<String, Map<String, String>> STYLES = new HashMap<>();

    private FontRegistry() {
    }

    /**
     * Set default font.
     * 
     * @param defaultFont default font
     */
	public static void setDefault(Font defaultFont) {
		FontRegistry.defaultFont = defaultFont;
	}

    /**
     * Get default font.
     * 
     * @return default font
     */
	public static Font getDefault() {
		return FontRegistry.defaultFont;
	}
	
    /**
     * Registers a loaded font as a concrete style of its family.
     *
     * @param loadedFont the font returned by {@link Font#loadFont(String, double)}
     */
    public static void register(Font loadedFont) {
        if (loadedFont == null) {
            return;
        }
        String family = loadedFont.getFamily();
        if (family == null || family.isBlank()) {
            family = loadedFont.getName();
        }
        String key = family.toLowerCase(Locale.ROOT);

        String styleKey = detectStyleKey(loadedFont.getName(), loadedFont.getStyle());
        STYLES.computeIfAbsent(key, _ -> new HashMap<>())
              .put(styleKey, loadedFont.getName());
    }

    /**
     * Resolves a concrete font name for the given base font and style.
     *
     * @param baseFont the base font (provides family and size)
     * @param bold     whether bold is requested
     * @param italic   whether italic is requested
     * @return a concrete font name, or {@code null} if none is registered
     */
    public static String resolveName(Font baseFont, boolean bold, boolean italic) {
        String family = baseFont.getFamily();
        String key = (family == null ? "" : family).toLowerCase(Locale.ROOT);
        Map<String, String> styles = STYLES.get(key);
        if (styles == null) {
            return null;
        }

        String styleKey;
        if (bold && italic) {
            styleKey = "bolditalic";
        } else if (bold) {
            styleKey = "bold";
        } else if (italic) {
            styleKey = "italic";
        } else {
            styleKey = "regular";
        }

        String name = styles.get(styleKey);
        if (name == null && italic) {
            name = styles.get(bold ? "bold" : "regular");
        }
        if (name == null && bold) {
            name = styles.get("regular");
        }
        return name;
    }

    private static String detectStyleKey(String name, String style) {
        String n = (name == null ? "" : name).toLowerCase(Locale.ROOT);
        String s = (style == null ? "" : style).toLowerCase(Locale.ROOT);
        boolean italic = n.contains("italic") || n.contains("oblique")
                || s.contains("italic") || s.contains("oblique");
        boolean bold = n.contains("bold") || n.contains("semibold") || n.contains("medium")
                || s.contains("bold") || s.contains("semibold") || s.contains("medium");

        if (bold && italic) {
            return "bolditalic";
        }
        if (bold) {
            return "bold";
        }
        if (italic) {
            return "italic";
        }
        return "regular";
    }

}