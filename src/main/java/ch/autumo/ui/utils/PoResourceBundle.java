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
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import ch.autumo.ui.Constants;

/**
 * Merged gettext {@link ResourceBundle}.
 *
 * <p>All texts of autumo-ui and of the running application are gettext PO
 * catalogues, {@code lang/messages.<lang>.po} below a base resource path. This bundle
 * merges the catalogues of the given base resource paths in the order they are passed
 * — the first catalogue wins — so an application only has to ship the PO files of its
 * own texts and everything else comes from the autumo-ui framework
 * ({@code /ch/autumo/ui/lang/messages.<lang>.po}). An application may nevertheless
 * override any framework text by defining its key.</p>
 *
 * <p>The PO files are parsed once per instance; autumo-ui caches the bundle per base
 * resource path and language ({@link Helper#getMessageBundle(String)}) and clears the
 * cache on a language change ({@link Helper#setLanguage(String)}). Because
 * {@link #handleGetObject(String)} returns {@code null} for an unknown key, callers can
 * apply their own fallback (see
 * {@link Helper#getText(ch.autumo.ui.fx.BaseApp, String, String)}) or simply show the
 * message id.</p>
 */
public final class PoResourceBundle extends ResourceBundle {

    /** Directory of the PO catalogues, relative to a base resource path. */
    private static final String PO_DIR = "lang/";

    /** File name prefix of a PO catalogue. */
    private static final String PO_PREFIX = "messages.";

    /** File name suffix of a PO catalogue. */
    private static final String PO_SUFFIX = ".po";

    /** All messages of the merged catalogues; the highest priority catalogue won. */
    private final Map<String, String> messages = new LinkedHashMap<String, String>();

    /**
     * Creates a bundle from the PO catalogues of the given base resource paths.
     *
     * @param lang language code, e.g. {@code en} or {@code de}
     * @param baseResourcePaths base resource paths in descending priority, e.g.
     *            {@code '/ch/autumo/app/'} and {@code '/ch/autumo/ui/'}
     */
    public PoResourceBundle(String lang, String... baseResourcePaths) {
        for (final String baseResourcePath : baseResourcePaths) {
            for (final Map.Entry<String, String> entry : load(baseResourcePath, lang).entrySet())
                this.messages.putIfAbsent(entry.getKey(), entry.getValue());
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns {@code null} when no merged catalogue defines the key.</p>
     */
    @Override
    protected Object handleGetObject(String key) {
        if (key == null)
            return null;
        final String translated = this.messages.get(key);
        return translated == null || translated.isEmpty() ? null : translated;
    }

    @Override
    public Enumeration<String> getKeys() {
        return Collections.enumeration(this.messages.keySet());
    }

    @Override
    public boolean containsKey(String key) {
        return key != null && this.messages.containsKey(key);
    }

    /**
     * Loads and parses the PO catalogue of one language below a base resource path.
     *
     * @param baseResourcePath base resource path, e.g. {@code /ch/autumo/app/}
     * @param lang language code
     * @return translation table; empty when there is no PO file or it cannot be read
     */
    private static Map<String, String> load(String baseResourcePath, String lang) {
        if (baseResourcePath == null || baseResourcePath.isEmpty())
            baseResourcePath = Constants.BASE_RESOURCE_PATH;
        if (!baseResourcePath.endsWith("/"))
            baseResourcePath += "/";
        String resource = (baseResourcePath + PO_DIR + PO_PREFIX + lang + PO_SUFFIX)
                .replace("//", "/");
        if (resource.startsWith("/"))
            resource = resource.substring(1);
        try (InputStream in = PoResourceBundle.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null)
                return Map.of();
            return PoFile.parse(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            return Map.of();
        }
    }
}
