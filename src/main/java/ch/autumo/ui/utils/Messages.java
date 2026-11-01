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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Map;

import ch.autumo.ui.fx.BaseApp;

/**
 * Thin i18n facade of the <b>autumo-ui</b> resource bundle.
 *
 * <p>The catalogue itself lives in autumo-ui: {@code Helper.getMessageBundle(…)} resolves the
 * gettext PO files ({@code lang/messages.<lang>.po}, see
 * {@link ch.autumo.ui.utils.PoResourceBundle}) first and falls back to the framework bundle for
 * the framework's own keys. This class therefore only adds the conventions that go beyond
 * that lookup:</p>
 *
 * <ul>
 *   <li>the message id of an untranslated text is <b>the English text itself</b> (gettext
 *       convention), so {@link #t(String, BaseApp)} falls back to the id;</li>
 *   <li>{@link #t(String, String, BaseApp)} lets metadata-driven texts (parameter labels, help texts) be
 *       translated by their raw configuration key while the English label stays the fallback;</li>
 *   <li>{@link #format(String, BaseApp, Object...)} applies {@link MessageFormat} and survives a malformed
 *       pattern;</li>
 *   <li>{@link #setLanguage(String, String)} selects the language and reports how many entries the
 *       catalogue carries.</li>
 * </ul>
 *
 * <p>The language is resolved once at start-up (see {@link ch.autumo.ui.fx.BaseApp#resolveLanguage(java.util.Properties)}: 
 * request parameter, configuration, desktop language) and can be switched at runtime.</p>
 */
public final class Messages {

    /** The source language of the application; also the fallback for missing translations. */
    public static final String LANG_EN = "en";

    private Messages() {
    }

    /**
     * The active language code, e.g. {@code en} or {@code de}.
     *
     * @return plain ISO 639-1 language code
     */
    public static String getLanguage() {
        return Helper.getLanguage();
    }
    
    /**
     * Switches the application language via the framework.
     *
     * <p>A region is ignored ({@code de_CH} and {@code de-CH} both select German) and unknown or
     * blank values fall back to English, exactly like {@link Helper#setLanguage(String)}. The
     * framework also moves the JVM default locale and the {@code user.language} system property,
     * so framework bundles and the per-language About HTML follow the choice.</p>
     *
     * @param lang language code, e.g. {@code en} or {@code de}
     * @param app base app
     * @return number of translations in the catalogue (0 for English apart from the framework-key
     *         overrides)
     */
    public static int setLanguage(String lang, BaseApp app) {
    	return setLanguage(lang, app.getBaseResourcePath());
    }

    /**
     * Translates {@code id} and returns {@code id} itself when it is not translated.
     *
     * @param id message id, which is the English text
     * @param app base app
     * @return translated text or the id
     */
    public static String t(String id, BaseApp app) {
        return t(id, id, app.getBaseResourcePath());
    }

    /**
     * Translates {@code id} and falls back to {@code english} when there is no translation.
     *
     * <p>This is what the metadata-driven parameter labels use: their <b>message id is the raw
     * configuration key</b> (e.g. {@code data_path}) and the fallback is the English label the code
     * derives from that key ({@code "Data path"}). A catalogue can then translate the parameter by
     * its key, and everything that is not translated stays as it is.</p>
     *
     * @param id message id, e.g. the raw configuration key
     * @param english English fallback text
     * @param app base app
     * @return translated text or {@code english}
     */
    public static String t(String id, String english, BaseApp app) {
        return t(id, english, app.getBaseResourcePath());
    }

    /**
     * Translates {@code id} and formats it with {@link MessageFormat} ({@code {0}}, {@code {1}}, …).
     *
     * @param id message id
     * @param app base app
     * @param args {@link MessageFormat} arguments
     * @return formatted text; the raw pattern when it is malformed
     */
    public static String format(String id, BaseApp app, Object... args) {
        return format(id, app.getBaseResourcePath(), args);
    }

    /**
     * Number of entries the PO catalogue of the active language carries.
     *
     * <p>English is the source language and ships only the framework-key overrides, so a value of
     * 0 means "no catalogue at all" (unknown language).</p>
     *
     * @param app base app
     * @return number of translated messages
     */
    public static int size(BaseApp app) {
    	return size(app.getBaseResourcePath());
    }
    
    /**
     * Switches the application language via the framework.
     *
     * <p>A region is ignored ({@code de_CH} and {@code de-CH} both select German) and unknown or
     * blank values fall back to English, exactly like {@link Helper#setLanguage(String)}. The
     * framework also moves the JVM default locale and the {@code user.language} system property,
     * so framework bundles and the per-language About HTML follow the choice.</p>
     *
     * @param lang language code, e.g. {@code en} or {@code de}
     * @param basePath base path, e.g. "ch/autumo/ui"
     * @return number of translations in the catalogue (0 for English apart from the framework-key
     *         overrides)
     */
    public static int setLanguage(String lang, String basePath) {
        Helper.setLanguage(lang);
        return size(basePath);
    }

    /**
     * Translates {@code id} and returns {@code id} itself when it is not translated.
     *
     * @param id message id, which is the English text
     * @param basePath base path, e.g. "ch/autumo/ui"
     * @return translated text or the id
     */
    public static String t(String id, String basePath) {
        return t(id, id, basePath);
    }

    /**
     * Translates {@code id} and falls back to {@code english} when there is no translation.
     *
     * <p>This is what the metadata-driven parameter labels use: their <b>message id is the raw
     * configuration key</b> (e.g. {@code data_path}) and the fallback is the English label the code
     * derives from that key ({@code "Data path"}). A catalogue can then translate the parameter by
     * its key, and everything that is not translated stays as it is.</p>
     *
     * @param id message id, e.g. the raw configuration key
     * @param english English fallback text
     * @param basePath base path, e.g. "ch/autumo/ui"
     * @return translated text or {@code english}
     */
    public static String t(String id, String english, String basePath) {
        final String translated = lookup(id, basePath);
        return translated == null || translated.isEmpty() ? english : translated;
    }

    /**
     * Translates {@code id} and formats it with {@link MessageFormat} ({@code {0}}, {@code {1}}, …).
     *
     * @param id message id
     * @param basePath base path, e.g. "ch/autumo/ui"
     * @param args {@link MessageFormat} arguments
     * @return formatted text; the raw pattern when it is malformed
     */
    public static String format(String id, String basePath, Object... args) {
        final String pattern = t(id, basePath);
        try {
            return MessageFormat.format(pattern, args);
        } catch (RuntimeException e) {
            // A malformed pattern must never break the UI; show the untranslated pattern.
            return pattern;
        }
    }

    /**
     * Number of entries the PO catalogue of the active language carries.
     *
     * <p>English is the source language and ships only the framework-key overrides, so a value of
     * 0 means "no catalogue at all" (unknown language).</p>
     *
     * @param basePath base path, e.g. "ch/autumo/ui"
     * @return number of translated messages
     */
    public static int size(String basePath) {
        return poEntries(Helper.getLanguage(), basePath).size();
    }

    /**
     * Looks a message id up in the framework bundle.
     *
     * @param id message id
     * @param basePath base path, e.g. "ch/autumo/ui"
     * @return translated text or {@code null} when the key is unknown
     */
    private static String lookup(String id, String basePath) {
        final java.util.ResourceBundle bundle = Helper.getMessageBundle(basePath);
        return bundle != null && bundle.containsKey(id) ? bundle.getString(id) : null;
    }

    /**
     * Reads the PO catalogue of a language from the classpath.
     *
     * @param lang language code
     * @param basePath base path, e.g. "ch/autumo/ui"
     * @return translation table; empty when the file is missing or unreadable
     */
    private static Map<String, String> poEntries(String lang, String basePath) {
    	if (basePath.startsWith("/")) {
    		basePath = basePath.substring(1);
    	}
    	String file = "/lang/messages." + lang + ".po";
    	if (basePath.endsWith("/")) {
    		file = "lang/messages." + lang + ".po";
    	}
        final String resource = basePath + file;
        try (InputStream in = Messages.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                return Map.of();
            }
            return PoFile.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            // A broken translation file degrades to English rather than to a crash.
            return Map.of();
        }
    }
}
