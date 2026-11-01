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

import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Style;
import org.fife.ui.rsyntaxtextarea.SyntaxScheme;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rsyntaxtextarea.Token;
import org.fife.ui.rtextarea.RTextScrollPane;

import ch.autumo.ui.fx.BaseApp;
import ch.autumo.ui.fx.controller.DefaultCodeEditorController;
import ch.autumo.ui.fx.model.DefaultPaneModel;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

/**
 * Default code editor view built on RSyntaxTextArea.
 *
 * <p>This is the JavaFX counterpart of the reference code editor: the <b>same component the
 * reference used</b> (RSyntaxTextArea in an RTextScrollPane), embedded into JavaFX with a
 * {@link SwingNode}. That is deliberate - the editor look of the reference (background,
 * gutter with line numbers and fold indicator, the fine gutter border, the caret and the token
 * colours) is defined by the RSyntaxTextArea theme {@code cfg/edit.xml}, which ships with
 * this framework and originates from the reference theme (only the gutter background,
 * the gutter divider and the scrollbars — {@link DarkScrollBarUI} — were adjusted for this
 * framework).</p>
 *
 * <p>The pane chrome (header with title and icon, body, footer with note label and buttons) is
 * not built here: it comes from the FXML ({@code codeEditorPane.fxml}), exactly as for every other
 * pane. An instance is created with {@link #DefaultCodeEditorView(BaseApp, Stage)} or
 * {@link #DefaultCodeEditorView(BaseApp, Stage, String)} and is afterwards set up with
 * {@link ch.autumo.ui.utils.UI#setup(BaseApp, String, ch.autumo.ui.fx.view.BaseBorderView, String)},
 * which loads the FXML onto this view and binds the controller.</p>
 *
 * <p>Title, icon, footer text and the additional footer buttons are not passed in a constructor:
 * they are set on the controller before the stage is shown, see
 * {@link DefaultCodeEditorController#customize(String, String, String)} and
 * {@link DefaultCodeEditorController#setButtons(java.util.List)}.</p>
 *
 * <p>Syntax highlighting is RSyntaxTextArea's own tokenizer; the language is selected with
 * {@link #setSyntaxEditingStyle(String)} using the {@code SyntaxConstants} values (JSON, YAML,
 * properties, plain text, ...). It is never guessed from the content - {@link #setSourceFile(Path)}
 * derives it from the file name's extension, see {@link #syntaxStyleForExtension(String)}; an
 * application adds its own formats with {@link #registerSyntaxStyle(String, String)}.</p>
 *
 * <p>Extensible by subclassing: {@link #createTextArea()}, {@link #createScrollPane(RSyntaxTextArea)}
 * and {@link #applyEditorTheme()} are protected hooks an application can override to change the
 * editor's behaviour or look. Note that all of them are invoked from the constructor, so overrides
 * must build their object from the hook parameters and base-class state only - subclass fields are
 * not yet initialised at that point.</p>
 *
 * <p><b>Two things an application that uses the editor has to know:</b></p>
 * <ul>
 *   <li>It embeds Swing, and the AWT event dispatch thread is not a daemon thread: it keeps the
 *       JVM alive after the JavaFX runtime is gone. The application ends the JVM itself and says
 *       so with {@link ch.autumo.ui.fx.BaseApp#isJvmExitOnStop()}.</li>
 *   <li>On macOS the AWT thread can block for ever in a native call while a Swing component is
 *       removed, and it holds the AWT tree lock while it does - a JavaFX thread that touches
 *       Swing in that moment waits for ever. The editor is therefore built on the AWT thread, the
 *       text is mirrored on the JavaFX side, and the JavaFX thread never waits for the AWT
 *       thread.</li>
 * </ul>
 *
 * <p>An application that has to know about the changes of the user overrides
 * {@link #textChanged()}; a text the application sets itself does not trigger it.</p>
 */
public class DefaultCodeEditorView extends BaseBorderView {

    /** CSS class of the whole pane, set by the FXML and used to scope the pane styles. */
    public static final String STYLE_CLASS = "code-editor-view";

    /** Resource path of the RSyntaxTextArea theme (the reference editor look). */
    private static final String THEME_RESOURCE = "/ch/autumo/ui/cfg/edit.xml";

    /** Editor text/caret colour before the theme is applied ({@code Constants.FG_COLOR}). */
    private static final java.awt.Color FG = new java.awt.Color(240, 240, 240);
    /** Editor background ({@code Constants.BG_COLOR}). */
    private static final java.awt.Color BG = new java.awt.Color(47, 47, 47);
    /** Editor frame colour ({@code Constants.BG_COLOR_FIELD}). */
    private static final java.awt.Color BORDER = new java.awt.Color(85, 85, 85);

    private final RSyntaxTextArea textArea;
    private final RTextScrollPane scrollPane;
    private final SwingNode swingNode;
    private final BorderPane editorPane;

    /** Whether the SwingNode already got its content (the embedded Swing editor). */
    private boolean contentSet = false;

    /**
     * Application-registered file-name extensions ("ifacex", "ifcx", ...) mapped to a
     * {@code SyntaxConstants} value. Checked before the built-in table, so an application
     * extends the editor with its own formats without the framework having to know them.
     */
    private final Map<String, String> extensionSyntaxStyles = new HashMap<>();

    /**
     * Set while the editor widget is filled with a text the application set: the document
     * listener must not report that as a change of the user.
     */
    private final java.util.concurrent.atomic.AtomicBoolean applying =
            new java.util.concurrent.atomic.AtomicBoolean();

    /**
     * Default code editor view.
     *
     * @param app the application
     * @param stage stage
     */
    public DefaultCodeEditorView(BaseApp app, Stage stage) {
        this(app, stage, null);
    }

    /**
     * Default code editor view.
     *
     * @param app the application
     * @param stage stage
     * @param styleResourcePath relative style ('style/style.css') file name or fully qualified resource path
     */
    public DefaultCodeEditorView(BaseApp app, Stage stage, String styleResourcePath) {
        super(app, stage, styleResourcePath);

        // Exactly the original sequence: editor settings, then the scroll pane (which creates the
        // line-number gutter), then the theme - the theme styles the gutter only when it exists.
        //
        // The editor is built on the calling (JavaFX) thread on purpose: the first Swing
        // component initializes the AWT graphics environment, and on macOS that initialization
        // (Metal) must not run on the AWT event dispatch thread - it blocks there. The AWT event
        // dispatch thread must in turn stay free, because Swing takes the AWT tree lock.
        this.textArea = createTextArea();
        // The document listener reports the changes of the user: an application that keeps the
        // edited text elsewhere - or that has to mark the document as modified - overrides
        // textChanged().
        this.textArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                changed();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                changed();
            }

            private void changed() {
                if (!DefaultCodeEditorView.this.applying.get()) {
                    DefaultCodeEditorView.this.textChanged();
                }
            }
        });
        this.scrollPane = createScrollPane(this.textArea);
        applyEditorTheme();

        this.swingNode = new SwingNode();
        this.editorPane = new BorderPane();
        this.editorPane.setCenter(this.swingNode);
        // The editing shortcuts (copy, paste, ...) of the Swing editor: inside a SwingNode a
        // Cmd/Ctrl combination does not reach the Swing component on every platform.
        EditorShortcuts.install(this.editorPane, this.textArea);
        // The SwingNode gets its content only when the pane is really used (see getEditorPane()):
        // setting the content shows the AWT window of the embedded Swing component, and that
        // window blocks the AWT event dispatch thread on macOS.

        final DefaultPaneModel model = new DefaultPaneModel(this);
        this.setModel(model);
        this.setController(new DefaultCodeEditorController(app, stage, model, this));

        // The editor gets the keyboard focus when the window opens, as in the reference.
        stage.showingProperty().addListener((_, _, showing) -> {
            if (!showing) {
                // The pane is closed: the editor is removed from the window. Switching the input
                // methods off keeps macOS from calling into the native input method while the
                // component is removed (Component.removeNotify()): that native call can block
                // the AWT thread forever, which freezes every Swing window of the application
                // and keeps the JVM from exiting. The work goes to the AWT event dispatch
                // thread and is not waited for: the JavaFX thread must not block on it.
                final RTextScrollPane pane = this.scrollPane;
                if (pane != null) {
                    SwingUtilities.invokeLater(() -> disableInputMethods(pane));
                }
                return;
            }
            Platform.runLater(() -> {
                this.swingNode.requestFocus();
                SwingUtilities.invokeLater(() -> this.textArea.requestFocusInWindow());
            });
        });
    }

    /**
     * Switches the input methods off for a component and all its children.
     *
     * <p>Every AWT component has the input methods enabled by default (see the event mask of
     * {@code java.awt.Component}), and while a component is removed macOS asks for its input
     * context: {@code Component.removeNotify()} calls {@code getInputContext()}, which ends in
     * the native input method of the operating system. That call can block the AWT event
     * dispatch thread forever - the application then freezes and the JVM does not exit. A
     * component without input methods is removed without that call.</p>
     *
     * <p>The whole hierarchy has to be covered, not only the text area: the gutter and its fold
     * indicator are removed as well, and they ask for their input context, too.</p>
     *
     * @param component the component and its children
     */
    public static void disableInputMethods(Component component) {
        if (component == null) {
            return;
        }
        component.enableInputMethods(false);
        if (component instanceof Container container) {
            for (final Component child : container.getComponents()) {
                disableInputMethods(child);
            }
        }
    }

    // ---------------- Subclass hooks ----------------

    /**
     * Create the editor widget: an RSyntaxTextArea with the settings the reference editor
     * had before its theme was applied (tab size, margin, line wrap, colours). The default syntax
     * style is plain text; subclasses/callers select a language with
     * {@link #setSyntaxEditingStyle(String)}.
     *
     * @return the editor widget
     */
    protected RSyntaxTextArea createTextArea() {
        final RSyntaxTextArea area = new RSyntaxTextArea();
        area.setTabSize(4);
        area.setEditable(true);
        area.setEnabled(true);
        area.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
        area.setForeground(FG);
        area.setCaretColor(FG);
        area.setBackground(BG);
        area.setMargin(new java.awt.Insets(8, 8, 8, 8));
        area.setLineWrap(true);
        area.revalidate();
        return area;
    }

    /**
     * Create the scroll pane around the editor widget: line-number gutter with fold indicator,
     * the fine frame of the reference ({@code Constants.BG_COLOR_FIELD}, 1px) and dark
     * scrollbars ({@link DarkScrollBarUI} - Swing's own look &amp; feel would paint light ones,
     * which clash with the dark editor theme).
     *
     * @param area the editor widget
     * @return the scroll pane holding the editor
     */
    protected RTextScrollPane createScrollPane(RSyntaxTextArea area) {
        // Inside a SwingNode the Swing content is painted off screen and copied into a JavaFX
        // texture on every repaint - the Swing double buffer is a second, redundant copy and
        // makes scrolling noticeably slower. Switching it off for the editor hierarchy is the
        // recommended setting for embedded Swing components.
        area.setDoubleBuffered(false);

        final RTextScrollPane pane = new RTextScrollPane(area);
        pane.setDoubleBuffered(false);
        pane.getViewport().setDoubleBuffered(false);
        pane.setFoldIndicatorEnabled(true);
        pane.setBorder(javax.swing.BorderFactory.createLineBorder(BORDER));
        // Dark scrollbars: Swing's own look & feel (Metal) would paint light grey ones,
        // which clash with the dark editor theme.
        pane.getVerticalScrollBar().setUI(new DarkScrollBarUI());
        pane.getHorizontalScrollBar().setUI(new DarkScrollBarUI());
        // The scroll pane itself and its viewport paint the corners around the bars.
        pane.setOpaque(true);
        pane.setBackground(new java.awt.Color(0x2f, 0x2f, 0x2f));
        pane.getViewport().setOpaque(true);
        pane.getViewport().setBackground(new java.awt.Color(0x2f, 0x2f, 0x2f));
        // A wheel notch moves more pixels: fewer repaints for the same distance.
        pane.getVerticalScrollBar().setUnitIncrement(32);
        pane.getHorizontalScrollBar().setUnitIncrement(32);
        return pane;
    }

    /**
     * Apply the RSyntaxTextArea theme and the token colours of the reference, in the theme
     * order: theme first, then the JSON panel's token colours, then the explicit overrides
     * (selection, current line, font, anti-aliasing).
     *
     * <p>The theme is applied <b>after</b> the scroll pane exists, because it also styles the
     * gutter: line numbers, gutter background, gutter border (the fine line right of the
     * numbers) and the fold indicator colours come from {@code cfg/edit.xml} - derived
     * from the file the reference shipped.</p>
     */
    protected void applyEditorTheme() {
        // The reference editor theme: dark background, light caret, grey line numbers,
        // the fine gutter border and the token colours - one file, applied as it always was.
        try (InputStream in = DefaultCodeEditorView.class.getResourceAsStream(THEME_RESOURCE)) {
            if (in != null)
                Theme.load(in).apply(this.textArea);
            else
                System.err.println("Editor theme not found: " + THEME_RESOURCE);
        } catch (IOException e) {
            System.err.println("Could not load the editor theme: " + e.getMessage());
        }

        // Token colours of the reference (JSONPanel), applied on top of the theme.
        final SyntaxScheme scheme = this.textArea.getSyntaxScheme();
        foreground(scheme, Token.COMMENT_EOL, new java.awt.Color(120, 120, 120));
        foreground(scheme, Token.RESERVED_WORD, new java.awt.Color(100, 100, 240));
        foreground(scheme, Token.RESERVED_WORD_2, new java.awt.Color(100, 100, 240));
        foreground(scheme, Token.REGEX, new java.awt.Color(102, 225, 248));
        foreground(scheme, Token.LITERAL_STRING_DOUBLE_QUOTE, new java.awt.Color(167, 236, 33));
        foreground(scheme, Token.OPERATOR, new java.awt.Color(240, 240, 240));
        foreground(scheme, Token.LITERAL_BOOLEAN, new java.awt.Color(240, 160, 50));
        foreground(scheme, Token.LITERAL_NUMBER_DECIMAL_INT, new java.awt.Color(240, 240, 40));
        foreground(scheme, Token.LITERAL_NUMBER_FLOAT, new java.awt.Color(240, 240, 40));
        foreground(scheme, Token.LITERAL_NUMBER_HEXADECIMAL, new java.awt.Color(240, 240, 40));

        this.textArea.setSelectionColor(new java.awt.Color(80, 80, 80));
        this.textArea.setCurrentLineHighlightColor(new java.awt.Color(55, 55, 55));
        this.textArea.setFont(new java.awt.Font(editorFontFamily(), java.awt.Font.BOLD, 14));
        this.textArea.setAntiAliasingEnabled(true);

        // The Swing popup menu follows the look & feel: paint it like the editor and use the
        // framework's font (Inter) for it.
        EditorPopup.applyDarkTheme(this.textArea);

        this.textArea.revalidate();
    }

    // ---------------- Public API ----------------

    /**
     * Get the editor widget wrapped in its JavaFX container; this is the node the controller
     * embeds into the pane body (see {@link DefaultCodeEditorController#init}). The container
     * holds a {@link SwingNode} with the RSyntaxTextArea scroll pane.
     *
     * @return the scrollable editor widget
     */
    public Pane getEditorPane() {
        if (!this.contentSet) {
            this.contentSet = true;
            this.swingNode.setContent(this.scrollPane);
        }
        return this.editorPane;
    }

    /**
     * Get the underlying editor widget.
     *
     * @return the code editor
     */
    public RSyntaxTextArea getTextArea() {
        return this.textArea;
    }

    /**
     * Get the swing node holding the editor, e.g. for focus handling.
     *
     * @return the swing node
     */
    public SwingNode getSwingNode() {
        return this.swingNode;
    }

    /**
     * Get the current editor text.
     *
     * @return the text
     */
    public String getText() {
        return this.textArea.getText();
    }

    /**
     * Set the editor text.
     *
     * <p>This is a change of the application, not of the user: {@link #textChanged()} is not
     * called for it.</p>
     *
     * @param text the text
     */
    public void setText(String text) {
        final String value = text == null ? "" : text;
        this.applying.set(true);
        try {
            this.textArea.setText(value);
        } finally {
            this.applying.set(false);
        }
    }

    /**
     * Called when the user changed the text in the editor.
     *
     * <p>The default does nothing. An application that keeps the edited text somewhere else, or
     * that has to mark the document as modified, overrides it:</p>
     *
     * <pre>
     * &#64;Override
     * protected void textChanged() {
     *     Platform.runLater(() -&gt; markModified());
     * }
     * </pre>
     *
     * <p>The call comes from the AWT event dispatch thread - the thread the editor lives on. An
     * application that touches the JavaFX scene has to hand the call over with
     * {@link javafx.application.Platform#runLater(Runnable)}.</p>
     */
    protected void textChanged() {
        // nothing to do by default
    }

    /**
     * Load a file into the editor: its content (read as UTF-8) becomes the editor text and the
     * syntax highlighting language is selected from the file name's extension, see
     * {@link #syntaxStyleForExtension(String)}. An unknown extension results in plain text
     * ({@link SyntaxConstants#SYNTAX_STYLE_NONE}) - the editor never guesses a language from the
     * content.
     *
     * <p>This is a convenience for the common "open this file" case; to set text and language
     * separately (e.g. generated content), use {@link #setText(String)} and
     * {@link #setSyntaxEditingStyle(String)} directly.</p>
     *
     * @param file file to show in the editor
     * @throws IOException if the file cannot be read
     */
    public void setSourceFile(Path file) throws IOException {
        final String content = Files.readString(file, StandardCharsets.UTF_8);
        setText(content);
        setSyntaxEditingStyle(syntaxStyleForExtension(file.getFileName().toString()));
    }

    /**
     * RSyntaxTextArea syntax style constant for a file name, derived from its extension
     * (case-insensitive). Config/data formats first - these are the files this framework's
     * editors are used for - then common source formats; everything else is plain text.
     *
     * <p>Extensions registered with {@link #registerSyntaxStyle(String, String)} are checked
     * first, so an application can add its own formats to this editor.</p>
     *
     * @param fileName file name (only the extension is looked at), may be {@code null}
     * @return the {@code SyntaxConstants} value, {@link SyntaxConstants#SYNTAX_STYLE_NONE} if the
     *         extension is unknown
     */
    public String syntaxStyleForExtension(String fileName) {
        if (fileName == null)
            return SyntaxConstants.SYNTAX_STYLE_NONE;
        final int dot = fileName.lastIndexOf('.');
        final String ext = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        final String registered = this.extensionSyntaxStyles.get(ext);
        if (registered != null)
            return registered;
        return syntaxStyleForExtensionDefault(ext);
    }

    /**
     * Registers a file-name extension for this editor, e.g. {@code registerSyntaxStyle("ifcx",
     * SyntaxConstants.SYNTAX_STYLE_YAML)}. Registered extensions are checked before the built-in
     * table and overwrite a previous registration of the same extension.
     *
     * @param extension file-name extension without dot, case-insensitive, e.g. {@code ifcx}
     * @param syntaxStyle the RSyntaxTextArea syntax style constant to use for it
     */
    public void registerSyntaxStyle(String extension, String syntaxStyle) {
        if (extension == null || extension.isBlank())
            throw new IllegalArgumentException("Extension must not be null or blank!");
        this.extensionSyntaxStyles.put(extension.strip().toLowerCase(Locale.ROOT), syntaxStyle);
    }

    /**
     * RSyntaxTextArea syntax style constant for a file-name extension (case-insensitive).
     * Config/data formats first - these are the files this framework's editors are used for -
     * then common source formats; everything else is plain text.
     *
     * @param fileName file name (only the extension is looked at), may be {@code null}
     * @return the {@code SyntaxConstants} value, {@link SyntaxConstants#SYNTAX_STYLE_NONE} if the
     *         extension is unknown
     */
    public static String syntaxStyleFor(String fileName) {
        if (fileName == null)
            return SyntaxConstants.SYNTAX_STYLE_NONE;
        final int dot = fileName.lastIndexOf('.');
        final String ext = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
        return syntaxStyleForExtensionDefault(ext);
    }

    /**
     * The built-in extension table: the formats the framework itself knows.
     *
     * @param ext lower-case file-name extension without dot
     * @return the {@code SyntaxConstants} value, {@link SyntaxConstants#SYNTAX_STYLE_NONE} if unknown
     */
    private static String syntaxStyleForExtensionDefault(String ext) {
        return switch (ext) {
            case "json" -> SyntaxConstants.SYNTAX_STYLE_JSON;
            case "jsonc" -> SyntaxConstants.SYNTAX_STYLE_JSON_WITH_COMMENTS;
            case "yaml", "yml" -> SyntaxConstants.SYNTAX_STYLE_YAML;
            case "properties", "cfg" -> SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE;
            case "xml", "fxml" -> SyntaxConstants.SYNTAX_STYLE_XML;
            case "csv" -> SyntaxConstants.SYNTAX_STYLE_CSV;
            case "ini", "conf" -> SyntaxConstants.SYNTAX_STYLE_INI;
            case "md", "markdown" -> SyntaxConstants.SYNTAX_STYLE_MARKDOWN;
            case "sh", "bash", "zsh" -> SyntaxConstants.SYNTAX_STYLE_UNIX_SHELL;
            case "bat", "cmd" -> SyntaxConstants.SYNTAX_STYLE_WINDOWS_BATCH;
            case "sql" -> SyntaxConstants.SYNTAX_STYLE_SQL;
            case "java" -> SyntaxConstants.SYNTAX_STYLE_JAVA;
            case "js", "mjs", "cjs" -> SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT;
            case "ts" -> SyntaxConstants.SYNTAX_STYLE_TYPESCRIPT;
            case "py" -> SyntaxConstants.SYNTAX_STYLE_PYTHON;
            case "rb" -> SyntaxConstants.SYNTAX_STYLE_RUBY;
            case "go" -> SyntaxConstants.SYNTAX_STYLE_GO;
            case "rs" -> SyntaxConstants.SYNTAX_STYLE_RUST;
            case "kt" -> SyntaxConstants.SYNTAX_STYLE_KOTLIN;
            case "scala" -> SyntaxConstants.SYNTAX_STYLE_SCALA;
            case "groovy", "gradle" -> SyntaxConstants.SYNTAX_STYLE_GROOVY;
            case "c", "h" -> SyntaxConstants.SYNTAX_STYLE_C;
            case "cpp", "cc", "cxx", "hpp" -> SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS;
            case "cs" -> SyntaxConstants.SYNTAX_STYLE_CSHARP;
            case "lua" -> SyntaxConstants.SYNTAX_STYLE_LUA;
            case "pl" -> SyntaxConstants.SYNTAX_STYLE_PERL;
            case "php" -> SyntaxConstants.SYNTAX_STYLE_PHP;
            case "css" -> SyntaxConstants.SYNTAX_STYLE_CSS;
            case "html", "htm" -> SyntaxConstants.SYNTAX_STYLE_HTML;
            case "dockerfile" -> SyntaxConstants.SYNTAX_STYLE_DOCKERFILE;
            default -> SyntaxConstants.SYNTAX_STYLE_NONE;
        };
    }

    /**
     * Select the syntax highlighting language, one of the {@code SyntaxConstants} values
     * ({@code SYNTAX_STYLE_JSON}, {@code SYNTAX_STYLE_YAML}, {@code SYNTAX_STYLE_PROPERTIES},
     * {@code SYNTAX_STYLE_NONE}, ...). RSyntaxTextArea highlights exactly one language at a time.
     *
     * @param syntaxStyle the RSyntaxTextArea syntax style constant
     */
    public void setSyntaxEditingStyle(String syntaxStyle) {
        this.textArea.setSyntaxEditingStyle(syntaxStyle);
    }

    /**
     * Move the caret to the document start, as the formatter did after formatting.
     */
    public void caretToStart() {
        this.textArea.setCaretPosition(0);
    }

    /**
     * Set the foreground colour of one token type, if the theme defined a style for it.
     *
     * @param scheme the syntax scheme of the editor
     * @param token  the token type, e.g. {@link Token#LITERAL_STRING_DOUBLE_QUOTE}
     * @param color  the foreground colour
     */
    private static void foreground(SyntaxScheme scheme, int token, java.awt.Color color) {
        final Style style = scheme.getStyle(token);
        if (style != null)
            style.foreground = color;
    }

    /**
     * Editor font family: the reference used Consolas on Windows and Monaco everywhere else.
     *
     * @return the editor font family
     */
    private static String editorFontFamily() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT).startsWith("windows")
                ? "Consolas" : "Monaco";
    }
}
