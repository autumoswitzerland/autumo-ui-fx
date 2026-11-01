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
package ch.autumo.ui.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.autumo.ui.fx.view.FxToolkit;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * Tests of the text of the "fine" dialogs: plain text or HTML.
 */
class FineDialogTextTest {

    /**
     * The JavaFX toolkit: the text nodes need it.
     */
    @BeforeAll
    static void startToolkit() {
        FxToolkit.start();
    }

    /**
     * Plain text becomes a label that wraps - no HTML parsing.
     */
    @Test
    void plainTextBecomesALabel() {
        final Node node = UI.fineDialogText("Nur Text, kein HTML.");
        assertInstanceOf(Label.class, node, "plain text is a label");
        assertTrue(node.getStyleClass().contains("fine-dialog-text"),
                "the text carries the style class of the dialogs");
    }

    /**
     * HTML becomes one text flow per line: the line breaks of the HTML.
     */
    @Test
    void htmlBecomesOneLinePerBreak() {
        final Node node = UI.fineDialogText("Erste Zeile<br>zweite Zeile<br>dritte Zeile");
        assertInstanceOf(VBox.class, node, "HTML is a box of lines");
        final VBox lines = (VBox) node;
        assertEquals(3, lines.getChildren().size(), "every line of the HTML is its own text flow");
        for (final Node line : lines.getChildren()) {
            assertInstanceOf(TextFlow.class, line, "a line is a text flow");
        }
    }

    /**
     * The tags of the HTML: bold, italic, underline and the colour of the font.
     */
    @Test
    void theTagsOfTheHtmlAreApplied() {
        final TextFlow flow = UI.htmlLine("a <b>fett</b> b <i>kursiv</i> c <u>unter</u> d");
        assertEquals(7, flow.getChildren().size(), "every piece of text is its own node");

        final Text bold = (Text) flow.getChildren().get(1);
        assertTrue(bold.getStyleClass().contains("fine-dialog-bold"), "the bold piece is bold");
        final Text italic = (Text) flow.getChildren().get(3);
        assertTrue(italic.getStyleClass().contains("fine-dialog-italic"), "the italic piece is italic");
        final Text underline = (Text) flow.getChildren().get(5);
        assertTrue(underline.getStyleClass().contains("fine-dialog-underline"),
                "the underlined piece is underlined");
    }

    /**
     * The colour of a font tag is set on the text.
     */
    @Test
    void theColourOfTheFontTagIsApplied() {
        final TextFlow flow = UI.htmlLine("normal <font color=\"#ff5555\">rot</font> normal");
        final Text red = (Text) flow.getChildren().get(1);
        assertEquals(Color.web("#ff5555"), red.getFill(), "the coloured piece has the colour");
    }

    /**
     * The HTML entities are replaced by their characters.
     */
    @Test
    void theHtmlEntitiesAreReplaced() {
        final TextFlow flow = UI.htmlLine("a &lt; b &amp; c &nbsp;d");
        assertEquals("a < b & c  d", ((Text) flow.getChildren().get(0)).getText(),
                "the entities are replaced");
    }

    /**
     * The detection of HTML: the tags the dialogs understand.
     */
    @Test
    void htmlIsRecognised() {
        assertTrue(UI.isHtml("<b>fett</b>"), "bold is HTML");
        assertTrue(UI.isHtml("Zeile<br>Zeile"), "a line break is HTML");
        assertTrue(UI.isHtml("<font color=\"#ff0000\">rot</font>"), "a colour is HTML");
        assertTrue(UI.isHtml("a &amp; b"), "an entity is HTML");
        assertFalse(UI.isHtml("Nur Text."), "plain text is not HTML");
        assertFalse(UI.isHtml(""), "an empty text is not HTML");
    }
}
