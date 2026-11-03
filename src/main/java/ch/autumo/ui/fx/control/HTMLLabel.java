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

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.autumo.ui.utils.FontRegistry;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

/**
 * A lightweight HTML label that mimics Swing's JLabel for basic HTML formatting.
 * Supports {@code <html>}, {@code <b>}, {@code <i>} and {@code <br>} tags.
 * <p>
 * This control does not load fonts itself. Bold and italic styles are resolved
 * through the application-wide {@link FontRegistry}, which must be populated
 * once during application startup for every loaded font file (e.g.
 * {@code Inter-Bold.ttf}, {@code Inter-Italic.ttf}). If no registered style
 * is found, the label falls back to JavaFX's built-in font factory.
 */
public class HTMLLabel extends TextFlow {

    /** Pattern matching supported HTML formatting tags. */
    private static final Pattern TAG_PATTERN =
            Pattern.compile("(?i)<(/?)(b|strong|i|em|br)\\s*/?>", Pattern.CASE_INSENSITIVE);

    private final ObjectProperty<Font> font =
            new SimpleObjectProperty<>(this, "font", FontRegistry.getDefault());

    private final StringProperty text =
            new SimpleStringProperty(this, "text", "");

    /**
     * Creates an empty HTML label.
     */
    public HTMLLabel() {
        super();
        font.addListener((_, _, _) -> updateTextFonts());
        text.addListener((_, _, newText) -> updateContent(newText));
    }

    /**
     * Creates an HTML label with the specified initial content.
     *
     * @param html the initial HTML content to display
     */
    public HTMLLabel(String html) {
        this();
        setText(html);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();

        for (Node node : getChildren()) {
            if (node instanceof Text text) {
                text.setStyle("-fx-fill: -fx-text-fill;");
            }
        }
    }

    // ---------------------------------------------------------------------
    // Properties
    // ---------------------------------------------------------------------

    /**
     * Returns the font property used by this label.
     *
     * @return the font property
     */
    public final ObjectProperty<Font> fontProperty() {
        return font;
    }

    /**
     * Sets the base font used to render the label text.
     *
     * @param font the font to use, or {@code null} to use the default font
     */
    public final void setFont(Font font) {
        this.font.set(font != null ? font : Font.getDefault());
    }

    /**
     * Returns the base font used to render the label text.
     *
     * @return the current font
     */
    public final Font getFont() {
        return font.get();
    }

    /**
     * Returns the text property for FXML compatibility.
     *
     * @return the text property
     */
    public final StringProperty textProperty() {
        return text;
    }

    /**
     * Sets the label content, interpreting HTML when the text starts with
     * an {@code <html>} tag. Otherwise, displays the content as plain text.
     *
     * @param text the text or HTML content to display, or {@code null} to clear the label
     */
    public final void setText(String text) {
        this.text.set(text);
    }

    /**
     * Returns the text or HTML content displayed by this label.
     *
     * @return the current text or HTML content
     */
    public final String getText() {
        return text.get();
    }

    /**
     * Sets the label content as HTML.
     *
     * @param html the HTML content to display, or {@code null} to clear the label
     */
    public void setHtml(String html) {
        text.set(html);
    }

    // ---------------------------------------------------------------------
    // Content parsing
    // ---------------------------------------------------------------------

    /**
     * Updates the displayed content, interpreting HTML when the text starts
     * with an {@code <html>} tag.
     *
     * @param value the text or HTML content to display
     */
    private void updateContent(String value) {
        getChildren().clear();

        if (value == null) {
            return;
        }

        if (!value.regionMatches(true, 0, "<html>", 0, 6)) {
            addText(value, false, false);
            return;
        }

        final String content = value.replaceFirst("(?is)^\\s*<html\\s*>", "")
                              .replaceFirst("(?is)</html>\\s*$", "");

        boolean bold = false;
        boolean italic = false;
        int last = 0;

        Matcher matcher = TAG_PATTERN.matcher(content);

        while (matcher.find()) {
            if (matcher.start() > last) {
                addText(content.substring(last, matcher.start()), bold, italic);
            }

            String tag = matcher.group(2).toLowerCase(Locale.ROOT);
            boolean closing = !matcher.group(1).isEmpty();

            switch (tag) {
                case "b", "strong" -> bold = !closing;
                case "i", "em" -> italic = !closing;
                case "br" -> addText("\n", bold, italic);
                default -> {
                }
            }

            last = matcher.end();
        }

        if (last < content.length()) {
            addText(content.substring(last), bold, italic);
        }
    }

    /**
     * Adds a text segment with the specified bold and italic formatting.
     *
     * @param value the text segment to display
     * @param bold whether the text should be bold
     * @param italic whether the text should be italic
     */
    private void addText(String value, boolean bold, boolean italic) {
        Text segment = new Text(value);
        segment.getProperties().put("html.bold", bold);
        segment.getProperties().put("html.italic", italic);
        
        // Set regular or bold font first, without passing italic
        segment.setFont(resolveFont(getFont(), bold, italic));
        
        // If italic is needed, simulate with Shear transformation
        if (italic) {
            segment.getTransforms().add(new javafx.scene.transform.Shear(-0.25, 0));
        }
        
        getChildren().add(segment);
    }

    /**
     * Updates the font of all existing text nodes while preserving their
     * bold and italic formatting.
     */
    private void updateTextFonts() {
        Font baseFont = getFont();

        for (Node node : getChildren()) {
            if (node instanceof Text segment) {
                boolean bold = Boolean.TRUE.equals(
                        segment.getProperties().get("html.bold"));
                boolean italic = Boolean.TRUE.equals(
                        segment.getProperties().get("html.italic"));

                segment.setFont(resolveFont(baseFont, bold, italic));
            }
        }

        requestLayout();
    }

    // ---------------------------------------------------------------------
    // Font resolution
    // ---------------------------------------------------------------------

    /**
     * Resolves a font for the given base font and style.
     * <p>
     * First asks the application-wide {@link FontRegistry} for a concrete
     * font name (e.g. {@code "Inter Italic"}). If none is registered, it
     * falls back to JavaFX's built-in font factory, which works for system
     * fonts that provide real style faces.
     *
     * @param baseFont the base font (provides family and size)
     * @param bold     whether bold is requested
     * @param italic   whether italic is requested
     * @return the resolved font
     */
    private Font resolveFont(Font baseFont, boolean bold, boolean italic) {
        String concreteName = FontRegistry.resolveName(baseFont, bold, italic);
        if (concreteName != null) {
            return Font.font(concreteName, baseFont.getSize());
        }

        return Font.font(
                baseFont.getFamily(),
                bold ? FontWeight.BOLD : FontWeight.NORMAL,
                italic ? FontPosture.ITALIC : FontPosture.REGULAR,
                baseFont.getSize()
        );
    }
}
