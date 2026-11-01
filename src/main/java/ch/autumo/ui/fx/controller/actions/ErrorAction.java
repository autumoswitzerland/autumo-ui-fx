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
package ch.autumo.ui.fx.controller.actions;

import java.io.PrintWriter;
import java.io.StringWriter;

import ch.autumo.ui.fx.control.HTMLLabel;
import javafx.geometry.Insets;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;

/**
 * Error action.
 */
public class ErrorAction extends BaseAction {

	private Throwable ex;
	private String msg;
	
	/**
	 * Error action.
	 * @param ex throwable
	 */
	public ErrorAction(Throwable ex) {
		this.ex = ex;
	}

	/**
	 * Error action.
	 * @param msg message
	 */
	public ErrorAction(String msg) {
		this.msg = msg;
	}
	
	@Override
	public void execute() {
		final BorderPane pane = controller.getBody();
		// Individual insets, depending on the element that is placed!
		BorderPane.setMargin(pane, new Insets(0, 6, 0, 6));
		// Default: show exception
		if (msg == null) {
			final StringWriter sw = new StringWriter();
			final PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			pw.flush();
			final String exceptionText = sw.toString();
			final TextArea ta = new TextArea();
			ta.setText(exceptionText);
			ta.getStyleClass().add("exception-text-area");
			pane.setCenter(ta);
		} else {
			final HTMLLabel label = new HTMLLabel();
			label.setText(msg);
			label.setStyle(this.getLabelStyle());
			pane.setTop(label);
		}
	}

	/**
	 * Get style for message label.
	 * 
	 * @return style
	 */
	public String getLabelStyle() {
		return "-fx-text-fill: #dddddd; -fx-font-size: 18px;";
	}	
}
