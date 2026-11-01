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

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.autumo.ui.Constants;
import javafx.geometry.VPos;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 * Icon nodes from the bundled Phosphor icon font (MIT, see
 * {@code fonts/Phosphor-LICENSE.txt}).
 *
 * <p>Phosphor ships 1530 icons; each one is addressed by its name ({@code "warning"},
 * {@code "trash"}, {@code "gear-six"}, {@code "folder-open"}, …). The name/codepoint
 * table {@code fonts/phosphor-codepoints.properties} is shipped with the font and is
 * generated from the upstream web package, because JavaFX does not apply the font's
 * ligatures reliably — the character code is therefore set explicitly.</p>
 *
 * <p>Usage:</p>
 *
 * <pre>
 * final Text icon = Icons.create("warning", 24);
 * if (icon != null) {
 *     icon.getStyleClass().add("ph-icon-warning");
 *     button.setGraphic(icon);
 * }
 * </pre>
 *
 * <p>Missing names and any font loading failure yield {@code null}, so a missing icon
 * never breaks a dialog or a toolbar. The base style sheet paints a plain
 * {@code .ph-icon} in the standard text colour; the semantic classes
 * {@code ph-icon-info}, {@code ph-icon-warning} and {@code ph-icon-error} carry the
 * colours used by the dialog types.</p>
 */
public final class Icons {

    private static final String FONT_RESOURCE = Constants.BASE_RESOURCE_PATH + "fonts/Phosphor.ttf";
    private static final String CODEPOINTS_RESOURCE = Constants.BASE_RESOURCE_PATH + "fonts/phosphor-codepoints.properties";

    private final static Logger LOG = LoggerFactory.getLogger(Icons.class.getName());

    private static boolean initialized = false;
    private static boolean available = false;
    private static String fontFamily = null;
    private static Map<String, Integer> codepoints = Map.of();

    private Icons() {
    }

    /**
     * Create an icon node at the default size (16px).
     *
     * @param name icon name
     * @return the icon node, or {@code null} if the name is unknown or the font cannot be loaded
     */
    public static Text create(String name) {
        return create(name, 16);
    }

    /**
     * Create an icon node at the given size.
     *
     * @param name icon name
     * @param size font size in pixels
     * @return the icon node, or {@code null} if the name is unknown or the font cannot be loaded
     */
    public static Text create(String name, double size) {
        initialize();
        if (!available)
            return null;
        final Integer codepoint = codepoints.get(name);
        if (codepoint == null) {
            LOG.warn("Icons: unknown Phosphor icon '{}'!", name);
            return null;
        }
        final Text text = new Text(new String(Character.toChars(codepoint)));
        text.setFont(Font.font(fontFamily, size));
        text.setTextOrigin(VPos.CENTER);
        text.getStyleClass().add("ph-icon");
        return text;
    }

    /**
     * Whether the given icon name is known to the bundled font.
     *
     * @param name icon name
     * @return {@code true} if the icon can be created
     */
    public static boolean isAvailable(String name) {
        initialize();
        return available && codepoints.containsKey(name);
    }

    private static synchronized void initialize() {
        if (initialized)
            return;
        initialized = true;
        try (InputStream fontStream = Icons.class.getResourceAsStream(FONT_RESOURCE)) {
            if (fontStream == null) {
                LOG.warn("Icons: font resource '{}' not found!", FONT_RESOURCE);
                return;
            }
            final Font loaded = Font.loadFont(fontStream, 12);
            if (loaded == null) {
                LOG.warn("Icons: could not load the Phosphor font!");
                return;
            }
            fontFamily = loaded.getFamily();
        } catch (IOException e) {
            LOG.warn("Icons: could not load the Phosphor font!", e);
            return;
        }
        try (InputStream tableStream = Icons.class.getResourceAsStream(CODEPOINTS_RESOURCE)) {
            if (tableStream == null) {
                LOG.warn("Icons: codepoint table '{}' not found!", CODEPOINTS_RESOURCE);
                return;
            }
            final Properties properties = new Properties();
            properties.load(tableStream);
            final Map<String, Integer> map = new HashMap<>(properties.size());
            for (String name : properties.stringPropertyNames()) {
                try {
                    map.put(name, Integer.parseInt(properties.getProperty(name), 16));
                } catch (NumberFormatException e) {
                    LOG.warn("Icons: invalid codepoint for '{}'!", name);
                }
            }
            codepoints = map;
            available = !map.isEmpty();
        } catch (IOException e) {
            LOG.warn("Icons: could not load the Phosphor codepoint table!", e);
        }
    }
}