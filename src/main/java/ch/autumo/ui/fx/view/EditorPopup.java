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

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.basic.BasicMenuItemUI;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

/**
 * Styles the Swing popup menu of an {@link RSyntaxTextArea} in the dark theme of the framework and
 * with the framework's UI font.
 *
 * <p>RSyntaxTextArea brings its own cut/copy/paste/select-all menu, but it is painted in the
 * colours of the Swing look &amp; feel - a bright menu on the dark editor. The menu itself is
 * reused; only its colours and the font are set, so the localized labels and the
 * enabled/disabled state handling of the component stay untouched.</p>
 *
 * <p>The framework loads Inter for JavaFX only; AWT does not know it. The font of the resources is
 * therefore loaded directly and used for the menu - no installation needed.</p>
 */
public final class EditorPopup {

	/** Background - the same colour the editor theme uses. */
	private static final Color BG = new Color(47, 47, 47);
	/** Foreground. */
	private static final Color FG = new Color(240, 240, 240);
	/** Colour of a disabled entry. */
	private static final Color DISABLED = new Color(130, 130, 130);
	/**
	 * Background of a hovered (armed) entry. The menu itself is very dark, so the hover has to be
	 * clearly lighter to be visible at all.
	 */
	private static final Color HOVER = new Color(82, 82, 82);
	/** Border around the menu. */
	private static final Color BORDER = new Color(85, 85, 85);

	/** Resource of the framework's Inter font. */
	private static final String INTER_RESOURCE = "/ch/autumo/ui/fonts/Inter-Regular.ttf";
	/** Font size of the menu entries. */
	private static final float FONT_SIZE = 13f;

	/** The Inter font for Swing, loaded on first use. */
	private static Font inter;

	private EditorPopup() {
	}

	/**
	 * The framework's UI font (Inter) for Swing components.
	 *
	 * @return the font at menu size
	 */
	public static Font font() {
		if (inter == null) {
			try (InputStream in = EditorPopup.class.getResourceAsStream(INTER_RESOURCE)) {
				inter = in != null
						? Font.createFont(Font.TRUETYPE_FONT, in)
						: new Font(Font.SANS_SERIF, Font.PLAIN, (int) FONT_SIZE);
			} catch (Exception e) {
				inter = new Font(Font.SANS_SERIF, Font.PLAIN, (int) FONT_SIZE);
			}
		}
		return inter.deriveFont(FONT_SIZE);
	}

	/**
	 * Paints the popup menu of the editor in the dark theme and in the framework font.
	 *
	 * <p>The menu of RSyntaxTextArea is created lazily; calling this method creates it, so the
	 * styling applies to the instance that is shown later.</p>
	 *
	 * @param area the editor whose menu is styled
	 */
	public static void applyDarkTheme(RSyntaxTextArea area) {
		final Font font = font();

		// The look & feel reads these defaults when a menu item is created, and the items are
		// created with the (lazily built) popup menu of the editor. They must therefore be set
		// BEFORE the menu is requested - setting them afterwards has no effect and the items
		// keep the bright hover/selection colours of the LAF.
		installDefaults(font);

		final JPopupMenu menu = area.getPopupMenu();
		if (menu == null)
			return;

		// An already existing menu was created with the former defaults: rebuild its UIs so the
		// dark defaults apply to it as well.
		SwingUtilities.updateComponentTreeUI(menu);

		menu.setBackground(BG);
		menu.setForeground(FG);
		menu.setFont(font);
		menu.setOpaque(true);
		menu.setBorder(BorderFactory.createLineBorder(BORDER));
		for (final Component component : menu.getComponents()) {
			if (component instanceof JMenuItem item) {
				item.setBackground(BG);
				item.setForeground(FG);
				item.setFont(font);
				item.setOpaque(true);
				// The hover is painted by the menu item UI itself; the platform look & feel may
				// ignore the selection colours from the UIManager, so it is set explicitly.
				item.setUI(new DarkMenuItemUI());
			} else if (component instanceof JSeparator separator) {
				separator.setBackground(BORDER);
				separator.setForeground(BORDER);
			}
		}
	}

	/**
	 * Installs the dark colours and the framework font as look & feel defaults of the menu.
	 *
	 * <p>Includes the accelerator colours (the keyboard shortcuts on the right of an entry) and
	 * the selection colours used while an entry is hovered - the LAF paints both itself.</p>
	 *
	 * @param font the framework font
	 */
	private static void installDefaults(Font font) {
		UIManager.put("MenuItem.background", BG);
		UIManager.put("MenuItem.foreground", FG);
		UIManager.put("MenuItem.disabledForeground", DISABLED);
		UIManager.put("MenuItem.selectionBackground", HOVER);
		UIManager.put("MenuItem.selectionForeground", FG);
		UIManager.put("MenuItem.acceleratorForeground", DISABLED);
		UIManager.put("MenuItem.acceleratorSelectionForeground", FG);
		UIManager.put("MenuItem.font", font);
		UIManager.put("PopupMenu.background", BG);
		UIManager.put("PopupMenu.foreground", FG);
		UIManager.put("PopupMenu.border", BorderFactory.createLineBorder(BORDER));
		UIManager.put("PopupMenu.font", font);
	}

	/**
	 * Menu item UI of the dark editor menu: paints the plain background and a clearly lighter one
	 * while the entry is hovered (armed) or selected.
	 *
	 * <p>{@link BasicMenuItemUI} is used deliberately - the platform look &amp; feel of macOS
	 * paints its own hover and would ignore the colours of the UIManager.</p>
	 */
	private static final class DarkMenuItemUI extends BasicMenuItemUI {

		@Override
		protected void paintBackground(Graphics g, JMenuItem item, Color bgColor) {
			g.setColor(item.isArmed() || item.isSelected() ? HOVER : BG);
			g.fillRect(0, 0, item.getWidth(), item.getHeight());
		}

		@Override
		protected void paintMenuItem(Graphics g, JComponent c, javax.swing.Icon checkIcon,
				javax.swing.Icon arrowIcon, Color background, Color foreground, int gap) {
			final JMenuItem item = (JMenuItem) c;
			super.paintMenuItem(g, c, checkIcon, arrowIcon, background,
					item.isEnabled() ? FG : DISABLED, gap);
		}

		@Override
		protected void installDefaults() {
			super.installDefaults();
			// the accelerator (shortcut) text sits on the right of an entry
			acceleratorForeground = DISABLED;
			acceleratorSelectionForeground = FG;
		}
	}
}
