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
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicScrollBarUI;

/**
 * Dark Scrollbar UI for the embedded Swing editor widgets (RSyntaxTextArea).
 *
 * <p>Swing uses the current look &amp; feel for scrollbars and, embedded in JavaFX, that is the
 * light Metal look - which clashes with the dark editor theme ({@code cfg/edit.xml}).
 * This UI paints a dark track and a flat grey thumb and hides the arrow buttons, as a modern
 * code editor does. It is applied by {@link DefaultCodeEditorView#createScrollPane} to both
 * scrollbars of the editor's {@code RTextScrollPane}.</p>
 *
 * <p>The colours match the editor theme: track = editor background, thumb = a grey derived
 * from the gutter divider.</p>
 */
public class DarkScrollBarUI extends BasicScrollBarUI {

    /** Default constructor; the UI has no configurable state. */
    public DarkScrollBarUI() {
    }

    /** Track colour (the editor background {@code 2f2f2f}). */
    private static final Color TRACK = new Color(0x2f, 0x2f, 0x2f);
    /** Thumb colour. */
    private static final Color THUMB = new Color(0x5a, 0x5a, 0x5a);
    /** Thumb colour while dragged or hovered. */
    private static final Color THUMB_ACTIVE = new Color(0x77, 0x77, 0x77);

    @Override
    protected void configureScrollBarColors() {
        super.configureScrollBarColors();
        this.thumbColor = THUMB;
        this.trackColor = TRACK;
        // Metal paints additional 3D shadows around the thumb; they are not used by
        // our own painting, but keep them dark in case a sub-delegate looks at them.
        this.thumbHighlightColor = THUMB;
        this.thumbLightShadowColor = THUMB;
        this.thumbDarkShadowColor = TRACK;
    }

    @Override
    protected JButton createDecreaseButton(int orientation) {
        return createInvisibleButton();
    }

    @Override
    protected JButton createIncreaseButton(int orientation) {
        return createInvisibleButton();
    }

    /**
     * Arrow buttons have no place in a code editor: zero-sized, invisible buttons.
     *
     * @return an invisible arrow button
     */
    private JButton createInvisibleButton() {
        final JButton button = new JButton();
        button.setPreferredSize(new Dimension(0, 0));
        button.setMinimumSize(new Dimension(0, 0));
        button.setMaximumSize(new Dimension(0, 0));
        button.setBorder(null);
        return button;
    }

    @Override
    public void installDefaults() {
        super.installDefaults();
        // Metal paints the areas outside the track (the ends of the bar, the corner next to the
        // other scrollbar) in its own light grey - they showed up as white patches.
        if (this.scrollbar != null) {
            this.scrollbar.setOpaque(true);
            this.scrollbar.setBackground(TRACK);
        }
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        // The whole bar gets the track colour first: BasicScrollBarUI only paints the track
        // area, everything around it (e.g. the ends of a horizontal bar) would keep the light
        // background of the look & feel.
        g.setColor(TRACK);
        g.fillRect(0, 0, c.getWidth(), c.getHeight());
        super.paint(g, c);
    }

    @Override
    protected void paintTrack(Graphics g, JComponent c, Rectangle trackBounds) {
        g.setColor(TRACK);
        g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height);
    }

    @Override
    protected void paintThumb(Graphics g, JComponent c, Rectangle thumbBounds) {
        // Nothing to paint while the scrollbar is hidden or the component is disabled.
        if (thumbBounds.isEmpty() || !c.isEnabled())
            return;

        g.setColor((scrollbar != null && scrollbar.getValueIsAdjusting()) || isThumbRollover()
                ? THUMB_ACTIVE : THUMB);
        g.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height);
    }

}
