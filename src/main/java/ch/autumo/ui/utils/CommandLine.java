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

import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Generic command-line handling for an autumo-ui application.
 *
 * <p>An application accepts a file to open in two shapes:</p>
 * <ul>
 *   <li>{@code --open <file>} — the explicit form;</li>
 *   <li>{@code <file>} — the bare file path a packaged ({@code jpackage}) launcher receives when
 *       the user double-clicks a registered file or uses <em>Open With</em>. The OS hands the
 *       selected file straight to the application as the first argument, so no option is
 *       present.</li>
 * </ul>
 *
 * <p>A bare argument is only treated as a file when its name carries one of the
 * <em>registered</em> extensions. The framework registers none by itself: an application declares
 * which file endings it can open via
 * {@link ch.autumo.ui.fx.BaseApp#registerOpenExtension(String)}. Without any registration no bare
 * argument is ever accepted, so unrelated arguments (JavaFX or JVM options) are never mistaken
 * for a file. The explicit {@code --open} form accepts any path, because the intent is then
 * unambiguous.</p>
 */
public final class CommandLine {

    /** Option that explicitly names the file to open. */
    public static final String OPEN_OPTION = "--open";

    private CommandLine() {
    }

    /**
     * Whether a file name carries one of the given extensions.
     *
     * @param name       file name or path to inspect (may be {@code null})
     * @param extensions registered extensions, each with a leading dot (e.g. {@code ".ifcx"}); may
     *                   be {@code null} or empty, in which case nothing matches
     * @return {@code true} if the name ends with one of the extensions (case-insensitive)
     */
    public static boolean hasExtension(String name, Collection<String> extensions) {
        if (name == null || extensions == null || extensions.isEmpty()) {
            return false;
        }
        final String lower = name.toLowerCase(Locale.ROOT);
        for (final String ext : extensions) {
            if (ext == null || ext.isEmpty()) {
                continue;
            }
            final String e = ext.toLowerCase(Locale.ROOT);
            if (lower.endsWith(e.startsWith(".") ? e : "." + e)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Picks the file to open from the raw command line arguments.
     *
     * <p>Recognises {@code --open <file>} and a bare {@code <file>} whose extension is registered
     * (see the class comment). Any other argument — notably one starting with {@code -} such as
     * {@code --verbose} — yields {@code null}, so the caller can keep handling its own
     * options.</p>
     *
     * @param args       raw command line arguments (may be {@code null} or empty)
     * @param extensions registered extensions, each with a leading dot; may be {@code null} or
     *                   empty
     * @return the file to open, or {@code null} when the command line carries none
     */
    public static Path openTarget(List<String> args, Collection<String> extensions) {
        if (args == null || args.isEmpty()) {
            return null;
        }
        final boolean explicit = OPEN_OPTION.equals(args.get(0));
        final int idx = explicit ? 1 : 0;
        if (args.size() <= idx) {
            return null;
        }
        final String candidate = args.get(idx);
        if (candidate == null || candidate.isEmpty() || candidate.startsWith("-")) {
            return null;
        }
        if (!explicit && !hasExtension(candidate, extensions)) {
            return null;
        }
        return Path.of(candidate);
    }
}
