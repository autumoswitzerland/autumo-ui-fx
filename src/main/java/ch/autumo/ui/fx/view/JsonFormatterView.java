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
package ch.autumo.ui.fx.view;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;

import ch.autumo.ui.fx.BaseApp;
import javafx.stage.Stage;

/**
 * JSON formatter editor view built on RSyntaxTextArea.
 *
 * <p>This is a specialised {@link DefaultCodeEditorView} for JSON documents: the syntax highlighting
 * is RSyntaxTextArea's own JSON tokenizer with the token colours of the theme (see
 * {@link DefaultCodeEditorView#applyEditorTheme()}), and documents are pretty-printed with Gson -
 * the same JSON library and formatting ({@code JsonParser.parseString} +
 * {@code setPrettyPrinting}). The header shows the {@code brackets-curly} glyph, set by the
 * controller when the pane is customized.</p>
 *
 * <p>The view is created with its two standard constructors, like every other view, and set up
 * with {@link ch.autumo.ui.utils.UI#setup}. It has no buttons of its own: the footer buttons are
 * set on the controller before the stage is shown, see
 * {@link ch.autumo.ui.fx.controller.DefaultCodeEditorController#setButtons(java.util.List)}. The
 * formatter window of the framework ({@code ch.autumo.ui.utils.UI#showJsonFormatter}) ships the
 * standard button set: Copy, Clear, Format JSON and Alright.</p>
 *
 * <p>Extensible by subclassing: the protected hooks of {@link DefaultCodeEditorView}
 * ({@code createTextArea}, {@code createScrollPane}, {@code applyEditorTheme}) change the editor's
 * behaviour or look.</p>
 */
public class JsonFormatterView extends DefaultCodeEditorView {

	/**
	 * Logger.
	 */
	protected static final Logger LOG = LoggerFactory.getLogger(JsonFormatterView.class.getName());
	
    /**
     * JSON formatter view.
     *
     * @param app the application
     * @param stage stage
     */
    public JsonFormatterView(BaseApp app, Stage stage) {
        this(app, stage, null);
    }

    /**
     * JSON formatter view.
     *
     * @param app the application
     * @param stage stage
     * @param styleResourcePath relative style ('style/style.css') file name or fully qualified resource path
     */
    public JsonFormatterView(BaseApp app, Stage stage, String styleResourcePath) {
        super(app, stage, styleResourcePath);
        // JSON highlighting is RSyntaxTextArea's own tokenizer.
        setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
    }

    /**
     * Format the current document: pretty-print it with Gson and move the
     * caret to the document start.
     *
     * <p>An empty document is formatted to an empty document; a document that
     * cannot be parsed is left untouched. The parse error is printed to the
     * console; the caller may show a dialog ("…couldn't be parsed… Check
     * console output.").</p>
     *
     * @return {@code true} if the document was formatted or empty, {@code false}
     *         if it couldn't be parsed as JSON
     */
    public boolean format() {
        final String text = getText();
        if (text == null || text.trim().isEmpty()) {
            setText("");
            return  true;
        }
        try {
            final String formatted = new GsonBuilder().setPrettyPrinting().create()
                    .toJson(JsonParser.parseString(text));
            setText(formatted);
            caretToStart();
            return  true;
        } catch (Exception e) {
            LOG.warn("The JSON result couldn't be parsed from the interface; "
                    + "the syntax seems corrupted! Check console output.");
            return false;
        }
    }
}
