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

import javax.swing.SwingUtilities;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;

/**
 * The editing shortcuts of a Swing editor inside a {@link javafx.embed.swing.SwingNode}.
 *
 * <p>A Cmd/Ctrl combination does not reach the Swing component on every platform: JavaFX handles the
 * key event first. The shortcuts are therefore intercepted on the JavaFX node and executed on the
 * editor directly. The <b>system clipboard of JavaFX</b> is used instead of the clipboard of Swing,
 * so copying in another application and pasting into the editor works - and the other way round.</p>
 *
 * <p>Handled are Cmd/Ctrl+C (copy), +V (paste), +X (cut), +A (select all), +Z (undo) and Shift+Z
 * (redo). Paste, cut and undo/redo do nothing when the editor is not editable.</p>
 */
public final class EditorShortcuts {

	private EditorShortcuts() {
	}

	/**
	 * Intercepts the editing shortcuts of the editor.
	 *
	 * @param root the JavaFX node that contains the {@code SwingNode} of the editor
	 * @param area the editor widget
	 */
	public static void install(final Pane root, final RSyntaxTextArea area) {
		if (root == null || area == null)
			return;

		root.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
			if (!event.isShortcutDown() || event.isAltDown())
				return;

			switch (event.getCode()) {
				case C -> {
					SwingUtilities.invokeLater(() -> copyToClipboard(area.getSelectedText()));
					event.consume();
				}
				case V -> {
					if (!area.isEditable())
						return;
					final String text = Clipboard.getSystemClipboard().getString();
					if (text == null || text.isEmpty())
						return;
					SwingUtilities.invokeLater(() -> area.replaceSelection(text));
					event.consume();
				}
				case X -> {
					if (!area.isEditable())
						return;
					SwingUtilities.invokeLater(() -> {
						final String selected = area.getSelectedText();
						copyToClipboard(selected);
						if (selected != null && !selected.isEmpty())
							area.replaceSelection("");
					});
					event.consume();
				}
				case A -> {
					SwingUtilities.invokeLater(area::selectAll);
					event.consume();
				}
				case Z -> {
					if (!area.isEditable())
						return;
					SwingUtilities.invokeLater(
							event.isShiftDown() ? area::redoLastAction : area::undoLastAction);
					event.consume();
				}
				default -> {
					// all other shortcuts belong to the editor
				}
			}
		});
	}

	/**
	 * Puts text into the system clipboard ({@link Clipboard} must be used on the JavaFX thread).
	 *
	 * @param text the text, ignored when empty
	 */
	private static void copyToClipboard(final String text) {
		if (text == null || text.isEmpty())
			return;
		javafx.application.Platform.runLater(() -> {
			final ClipboardContent content = new ClipboardContent();
			content.putString(text);
			Clipboard.getSystemClipboard().setContent(content);
		});
	}
}
