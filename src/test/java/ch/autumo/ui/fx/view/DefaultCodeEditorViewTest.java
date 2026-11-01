/**
 * Copyright 2023 autumo GmbH, Michael Gasche.
 * All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of autumo GmbH The intellectual and technical
 * concepts contained herein are proprietary to autumo GmbH
 * and are protected by trade secret or copyright law.
 *
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from autumo GmbH.
 */
package ch.autumo.ui.fx.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * Tests of the code editor.
 *
 * <p>The editor is a Swing component (RSyntaxTextArea) in a JavaFX window - the tests build it on
 * the JavaFX thread, exactly as the framework does it when it opens a pane.</p>
 */
class DefaultCodeEditorViewTest {

    /**
     * Starts the JavaFX toolkit: the editor needs a stage.
     *
     * @throws Exception if the toolkit does not start
     */
    @BeforeAll
    static void startToolkit() throws Exception {
        FxToolkit.start();
    }

    /**
     * The code editor's file convenience: {@code setSourceFile} reads the file as UTF-8, shows its
     * content and picks the highlighting language from the file name extension - the caller never
     * has to set the syntax style by hand. An unknown extension falls back to plain text; the
     * language is never guessed from the content.
     *
     * @throws Exception if the temporary files cannot be written
     */
    @Test
    void codeEditorShowsAFileAndHighlightsItAccordingToItsExtension() throws Exception {
        final Path json = Files.createTempFile("autumo-editor-test", ".json");
        final Path yaml = Files.createTempFile("autumo-editor-test", ".yml");
        final Path unknown = Files.createTempFile("autumo-editor-test", ".autumo");
        try {
            Files.writeString(json, "{\"a\": 1}");
            Files.writeString(yaml, "a: 1");
            Files.writeString(unknown, "just text");

            call(() -> {
                final DefaultCodeEditorView view = new DefaultCodeEditorView(null, new Stage(), null);
                try {
                    view.setSourceFile(json);
                    assertEquals("{\"a\": 1}", view.getText(), "the file content is shown");
                    assertEquals(SyntaxConstants.SYNTAX_STYLE_JSON,
                            view.getTextArea().getSyntaxEditingStyle(),
                            "a .json file is highlighted as JSON");

                    view.setSourceFile(yaml);
                    assertEquals(SyntaxConstants.SYNTAX_STYLE_YAML,
                            view.getTextArea().getSyntaxEditingStyle(),
                            "a .yml file is highlighted as YAML");

                    view.setSourceFile(unknown);
                    assertEquals(SyntaxConstants.SYNTAX_STYLE_NONE,
                            view.getTextArea().getSyntaxEditingStyle(),
                            "an unknown extension is shown as plain text");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        } finally {
            Files.deleteIfExists(json);
            Files.deleteIfExists(yaml);
            Files.deleteIfExists(unknown);
        }

        // The extension lookup itself: case-insensitive, framework formats only. An application
        // adds its own formats per editor (see the registration below).
        assertEquals(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE,
                DefaultCodeEditorView.syntaxStyleFor("app.PROPERTIES"));
        assertEquals(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE,
                DefaultCodeEditorView.syntaxStyleFor("app.cfg"),
                "the application configuration .cfg is Java properties");
        assertEquals(SyntaxConstants.SYNTAX_STYLE_XML,
                DefaultCodeEditorView.syntaxStyleFor("web.xml"));
        assertEquals(SyntaxConstants.SYNTAX_STYLE_NONE,
                DefaultCodeEditorView.syntaxStyleFor(null));
        assertEquals(SyntaxConstants.SYNTAX_STYLE_NONE,
                DefaultCodeEditorView.syntaxStyleFor("002-hubspot-v3-companies.ifacex"),
                "the framework does not know the Studio's interface formats");

        // An application adds its own formats per editor: the legacy .ifacex format is Java
        // properties, its successor format .ifcx is YAML; the registration wins over the
        // built-in table and is case-insensitive.
        call(() -> {
            final DefaultCodeEditorView view = new DefaultCodeEditorView(null, new Stage(), null);
            view.registerSyntaxStyle("ifacex", SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
            view.registerSyntaxStyle("ifcx", SyntaxConstants.SYNTAX_STYLE_YAML);

            assertEquals(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE,
                    view.syntaxStyleForExtension("002-hubspot-v3-companies.ifacex"),
                    "the legacy .ifacex interface format is Java properties");
            assertEquals(SyntaxConstants.SYNTAX_STYLE_YAML,
                    view.syntaxStyleForExtension("002-hubspot-v3-companies.IFCX"),
                    "the new .ifcx interface format is YAML");
            assertEquals(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE,
                    view.syntaxStyleForExtension("app.cfg"),
                    "the registration does not affect the built-in formats");
            assertEquals(SyntaxConstants.SYNTAX_STYLE_NONE,
                    view.syntaxStyleForExtension("unknown.txt"));
            assertThrows(IllegalArgumentException.class, () -> view.registerSyntaxStyle(" ", "x"));
            return null;
        });
    }

    /**
     * Runs the action on the JavaFX thread.
     *
     * @param <T> result type
     * @param callable callable to run
     * @return the result of the callable
     */
    private static <T> T call(java.util.concurrent.Callable<T> callable) {
        final FutureTask<T> task = new FutureTask<>(callable);
        Platform.runLater(task);
        try {
            return task.get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new IllegalStateException("JavaFX call failed: " + e, e);
        }
    }
}
