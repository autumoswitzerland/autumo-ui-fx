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

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import ch.autumo.ui.Constants;
import ch.autumo.ui.fx.BaseApp;
import ch.autumo.ui.fx.control.Icons;
import ch.autumo.ui.fx.controller.BaseController;
import ch.autumo.ui.fx.controller.DefaultCodeEditorController;
import ch.autumo.ui.fx.controller.DefaultInfoController;
import ch.autumo.ui.fx.controller.RegistrationController;
import ch.autumo.ui.fx.controller.actions.AboutWithLicenseAction;
import ch.autumo.ui.fx.controller.actions.Action;
import ch.autumo.ui.fx.controller.actions.CustomSizeAction;
import ch.autumo.ui.fx.controller.actions.ErrorAction;
import ch.autumo.ui.fx.controller.actions.HTMLAction;
import ch.autumo.ui.fx.controller.actions.RegistrationAction;
import ch.autumo.ui.fx.controller.actions.RemoveAction;
import ch.autumo.ui.fx.view.BaseAnchorView;
import ch.autumo.ui.fx.view.BaseBorderView;
import ch.autumo.ui.fx.view.BaseView;
import ch.autumo.ui.fx.view.DefaultInfoView;
import ch.autumo.ui.fx.view.JsonFormatterView;
import ch.autumo.ui.fx.view.RegistrationView;
import ch.autumo.ui.license.LicensedApplication;
import javafx.animation.FadeTransition;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * GUI/MVC Utility class.
 */
public class UI {

	private static final Logger LOG = LoggerFactory.getLogger(UI.class.getName());

	/** Width of the "fine" dialogs (the macOS look): they only grow in height. */
	private static final double DIALOG_WIDTH = 300;

	/** Padding between the content of a "fine" dialog and its border. */
	private static final double DIALOG_PADDING = 24;

	/** Distance between the parts of a "fine" dialog (glyph, text, buttons). */
	private static final double DIALOG_GAP = 24;

	/** Room around the dialog for its shadow. */
	private static final double DIALOG_SHADOW_ROOM = 12;
	
	/**
	 * The width the text of a "fine" dialog may take: the rest of the dialog besides the
	 * buttons.
	 *
	 * @param buttonsWidth the width the buttons take
	 * @return the width of the text
	 */
	private static final double DIALOG_TEXT_WIDTH = DIALOG_WIDTH - 2 * DIALOG_PADDING;

	/** Distance between the buttons of a "fine" dialog. */
	private static final double DIALOG_BUTTON_GAP = 10;

	/**
	 * The colour of the text of a "fine" dialog.
	 *
	 * <p>JavaFX has no built-in dark theme: the framework brings its own dark colours
	 * ({@code -fx-background: #222222}), but the default text colour stays black. The text of the
	 * dialogs is therefore set explicitly - and the pieces of text of the HTML are coloured on
	 * their own, because a text node does not inherit the fill colour of its parent.</p>
	 */
	private static final String DIALOG_TEXT_COLOR = "#dddddd";

	/** Colour of the circle behind the glyph of an information or confirmation dialog. */
	private static final String DIALOG_COLOR_INFO = "#0E4E74";

	/** Colour of the circle behind the glyph of a warning dialog. */
	private static final String DIALOG_COLOR_WARNING = "#885000";

	/** Colour of the circle behind the glyph of an error dialog. */
	private static final String DIALOG_COLOR_ERROR = "#aa0000";

	/** The HTML tags the "fine" dialogs understand. */
	private static final Pattern HTML_TAG = Pattern.compile("(?i)</?(b|i|u|font)[^>]*>");

	/** The name of an HTML tag, e.g. "font" for {@code <font color="#ff5555">}. */
	private static final Pattern HTML_TAG_NAME = Pattern.compile("(?i)^</?([a-z]+)");

	/** The colour of a font tag. */
	private static final Pattern HTML_COLOUR = Pattern.compile("(?i)color\\s*=\\s*[\"']?([#a-zA-Z0-9]+)[\"']?");
	
	/** resource base bath */
	private static String baseResourcePath = null;

	
	/**
	 * Default constructor.
	 */
	private UI() {
	}
	
	/**
	 * Setup a view with a controller.
	 * Tries to fetch the controller from the view!
	 * 
	 * @param app the application
	 * @param baseResourcePath base resource path
	 * @param view view
	 * @param fxmlName FXML file name
	 * @return controller
	 */
	public static BaseController setup(BaseApp app, String baseResourcePath, Pane view, String fxmlName) {
		return UI.setup(app, baseResourcePath, UI.getBaseController(view), view, fxmlName);
	}
	
	/**
	 * Setup a view with a controller.
	 * 
	 * @param app the application
	 * @param baseResourcePath base resource path
	 * @param controller controller
	 * @param view view
	 * @param fxmlName FXML file name
	 * @return controller
	 */
	public static BaseController setup(BaseApp app, String baseResourcePath, Object controller, Pane view, String fxmlName) {

		if (UI.baseResourcePath == null)
			UI.baseResourcePath = baseResourcePath;
		
		final boolean isMain = fxmlName.toLowerCase().contains("main.fxml");
		
		final URL fxmlUrl = view.getClass().getResource(fxmlName);
		if (fxmlUrl == null) {
			LOG.error("UI: Failed to locate FXML '" + fxmlName + "' for class '"+view.getClass()+"' !");
			throw new IllegalStateException("UI: Failed to locate FXML '" + fxmlName + "' for class '"+view.getClass()+"' !");
		}
		
		final FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl, Helper.getMessageBundle(baseResourcePath));
		
		fxmlLoader.setRoot(view);

		final BaseController baseController = (BaseController) controller;
		if (ConfigurationManager.getInstance().getYesOrNoNoWarn("disableMacOSMenuDetach"))
			baseController.disableMacOSMenuDetach();
		
		fxmlLoader.setController(baseController);

		try {
			fxmlLoader.load();
		} catch (IOException e) {
			LOG.error("UI: Failed to read FXML '" + fxmlName + "' for class '"+view.getClass()+"' !", e);
			throw new IllegalStateException("UI: Failed to read FXML '" + fxmlName + "' for class '"+view.getClass()+"' !", e);
		}
		
		
		// Install Default Shortcuts
		Method getS = null;
		Class<?> clz = view.getClass();
		try {
			if (BaseView.class.isAssignableFrom(clz))
				getS = BaseView.class.getDeclaredMethod("getStage");
			if (BaseBorderView.class.isAssignableFrom(clz))
				getS = BaseBorderView.class.getDeclaredMethod("getStage");
			if (BaseAnchorView.class.isAssignableFrom(clz))
				getS = BaseAnchorView.class.getDeclaredMethod("getStage");
		} catch (NoSuchMethodException e) {
			LOG.error("UI: 'getStage' method not found in class '"+clz+"'; the view must be sub-classed by a valid base view -> No keyboard shortcuts installed!", e);
			throw new RuntimeException("UI: 'getStage' method not found in class '"+clz+"'; the view must be sub-classed by a valid base view -> No keyboard shortcuts installed!", e);
		}
		final Stage stage;
		try {
			stage = (Stage) getS.invoke(view);
		} catch (Exception e) {
			LOG.error("UI: Couldn't invoke 'getStage' on class '"+clz+"' !", e);
			throw new RuntimeException("UI: Couldn't invoke 'getStage' on class '"+clz+"'; the view must be sub-classed by a valid base view -> No keyboard shortcuts installed!", e);
		}		
		
		
		StageStyle style = StageStyle.DECORATED;
		final String primaryStageStyle = ConfigurationManager.getInstance().getStringNoWarn("primaryStageStyle");
		if (primaryStageStyle != null) {
			switch (primaryStageStyle.toUpperCase()) {
				case "DECORATED":
					style = StageStyle.DECORATED;
					break;
				case "UNDECORATED":
					style = StageStyle.UNDECORATED;
					break;
				case "TRANSPARENT":
					style = StageStyle.TRANSPARENT;
					break;
				case "UTILITY":
					style = StageStyle.UTILITY;
					break;
				}
			stage.initStyle(style);
		}
		
		
		final Scene scene = stage.getScene();
		if (isMain) {
			// Only for main views!
			scene.setOnKeyPressed( key -> {
			    if (key.getCode() == KeyCode.L && key.isShiftDown() && key.isControlDown()) {
			    	Helper.openFile(OSUtils.getPropertiesPath(app.getInternalAppName()) + "logs"+"/"+app.getInternalAppName()+".log");
			    }
			    if (key.getCode() == KeyCode.C && key.isShiftDown() && key.isControlDown()) {
			    	Helper.openFile(OSUtils.getPropertiesPath(app.getInternalAppName()));
			    }
			    baseController.keyPressed(key);
			});
		} else {
			// For others
			scene.setOnKeyPressed( key -> {
			    if (key.getCode() == KeyCode.ESCAPE) {
			    	baseController.escKeyPressed(key);
			    }
			    baseController.keyPressed(key);
			});
		}
		
		return baseController;
	}

	/**
	 * Get base controller from view.
	 * 
	 * @param view view
	 * @return base controller
	 */
	public static BaseController getBaseController(Pane view) {
		
		Method getC = null;
		Class<?> clz = view.getClass();
		try {
			if (BaseView.class.isAssignableFrom(clz))
				getC = BaseView.class.getDeclaredMethod("getController");
			if (BaseBorderView.class.isAssignableFrom(clz))
				getC = BaseBorderView.class.getDeclaredMethod("getController");
			if (BaseAnchorView.class.isAssignableFrom(clz))
				getC = BaseAnchorView.class.getDeclaredMethod("getController");
		} catch (NoSuchMethodException e) {
			LOG.error("UI: 'getController' method not found in class '"+clz+"'; the view must be sub-classe by a valid base view!", e);
			throw new RuntimeException("UI: 'getController' method not found in class '"+clz+"'; the view must be sub-classe by a valid base view!", e);
		}
		
		Object controller = null;
		try {
			controller = getC.invoke(view);
		} catch (Exception e) {
			LOG.error("UI: Couldn't invoke 'getController' on class '"+clz+"'; the view must be sub-classed by a valid base view!", e);
			throw new RuntimeException("UI: Couldn't invoke 'getController' on class '"+clz+"'; the view must be sub-classed by a valid base view!", e);
		}
		
		return (BaseController) controller;
	}
	
	/**
	 * Get image.
	 * 
	 * @param imageFile image file
	 * @return image
	 */
	public static Image imageWithoutFileBlocking(Path imageFile) {
		try {
			return new Image(new ByteArrayInputStream(Files.readAllBytes(imageFile)));
		} catch (IOException e) {
			LOG.error("UI: Could not load image '" + imageFile + "' !", e);
			throw new IllegalStateException("UI: Could not load image '" + imageFile + "' !", e);
		}
	}

	/**
	 * Show fine confirmation dialog.
	 * 
	 * @param app the application
	 * @param question the question text, plain or HTML
	 * @return chosen option
	 */
	public static Optional<ButtonType> showFineWarningConfirmationDialog(BaseApp app, String question) {
		return showFineDialog(app, "warning", DIALOG_COLOR_WARNING, question, true);
	}

	/**
	 * Show fine confirmation dialog.
	 * 
	 * @param app the application
	 * @param question the question text, plain or HTML
	 * @return chosen option
	 */
	public static Optional<ButtonType> showFineConfirmationDialog(BaseApp app, String question) {
		return showFineDialog(app, "info", DIALOG_COLOR_INFO, question, true);
	}
	
	/**
	 * Show fine information dialog.
	 * 
	 * @param app the application
	 * @param text the information text, plain or HTML
	 */
	public static void showFineInformationDialog(BaseApp app, String text) {
		showFineDialog(app, "info", DIALOG_COLOR_INFO, text, false);
	}
	
	/**
	 * Show fine warning dialog.
	 * 
	 * @param app the application
	 * @param text the warning text, plain or HTML
	 */
	public static void showFineWarningDialog(BaseApp app, String text) {
		showFineDialog(app, "warning", DIALOG_COLOR_WARNING, text, false);
	}

	/**
	 * Show fine error dialog.
	 * 
	 * @param app the application
	 * @param text the error text, plain or HTML
	 */
	public static void showFineErrorDialog(BaseApp app, String text) {
		showFineDialog(app, "x-circle", DIALOG_COLOR_ERROR, text, false);
	}

	/**
	 * Shows a "fine" dialog: the look of the new macOS dialogs.
	 *
	 * <p>No window title and no frame - the dialog is a transparent window whose frame is drawn
	 * by the content (rounded corners, fixed width, the height grows with the text). The glyph
	 * comes on top in its coloured circle, the text below it, the buttons at the bottom right.</p>
	 *
	 * <p>The text is either plain text (a label that wraps) or HTML (a {@link TextFlow}, which
	 * understands a subset of HTML: bold, italic, underline, line breaks and colours).</p>
	 *
	 * @param app          the application
	 * @param icon         the icon of the glyph
	 * @param color        the colour of the circle behind the glyph
	 * @param text         the text, plain or HTML
	 * @param confirmation {@code true} for the confirmation dialog with a cancel button
	 * @return the chosen option, empty if the dialog was dismissed
	 */
	private static Optional<ButtonType> showFineDialog(BaseApp app, String icon, String color,
			String text, boolean confirmation) {

		final VBox frame = new VBox(DIALOG_GAP);
		frame.setAlignment(Pos.CENTER);
		frame.setPadding(new Insets(DIALOG_PADDING));
		// The dialog has a fixed width - it only grows in height.
		frame.setPrefWidth(DIALOG_WIDTH);
		frame.setMinWidth(DIALOG_WIDTH);
		frame.setMaxWidth(DIALOG_WIDTH);
		frame.getStyleClass().add("fine-dialog");

		// The glyph on top, in its coloured circle.
		final Text glyph = Icons.create(icon, 32);
		if (glyph != null) {
			glyph.setStyle("-fx-fill: #ffffff;");
			final StackPane box = new StackPane(glyph);
			box.setPrefSize(48, 48);
			box.setMinSize(48, 48);
			box.setMaxSize(48, 48);
			box.setAlignment(Pos.CENTER);
			box.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 24;");
			frame.getChildren().add(box);
		}

		// The text: HTML in a text flow, plain text in a label that wraps.
		final Region textNode = fineDialogText(text);
		textNode.setPrefWidth(DIALOG_TEXT_WIDTH);
		textNode.setMaxWidth(DIALOG_TEXT_WIDTH);
		frame.getChildren().add(textNode);

		// The buttons below the text: one below the other, the primary one ("Okay") on top, the
		// secondary one below it. They fill the width of the dialog - with a margin to the left
		// and to the right.
		final VBox buttons = new VBox(DIALOG_BUTTON_GAP);
		buttons.setAlignment(Pos.CENTER);
		final Button cancel = new Button(Helper.getTranslatedText(app, "button.Cancel"));
		final Button ok = new Button(Helper.getTranslatedText(app, "button.OK"));
		cancel.getStyleClass().add("fine-dialog-button-secondary");
		ok.getStyleClass().add("fine-dialog-button");
		buttons.getChildren().add(ok);
		if (confirmation) {
			buttons.getChildren().add(cancel);
		}
		frame.getChildren().add(buttons);
		final Stage stage = new Stage();
		// A transparent window: no title, no frame - the frame of the dialog is the content.
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.initModality(Modality.APPLICATION_MODAL);
		stage.setTitle("");
		stage.setResizable(false);
		
		// The room around the dialog for its shadow - the scene would cut it off otherwise.
	    final StackPane outer = new StackPane(frame);
	    outer.setStyle("-fx-background-color: transparent;");
	    outer.setPadding(new Insets(DIALOG_SHADOW_ROOM));

		final Scene scene = new Scene(outer);
		scene.setFill(Color.TRANSPARENT);
		scene.getStylesheets().addAll(Helper.getApplicationStyleSheets(app));
		stage.setScene(scene);

		// The chosen option.
		final boolean[] confirmed = { false };
		ok.setOnAction(_ -> {
			confirmed[0] = true;
			stage.close();
		});
		cancel.setOnAction(_ -> stage.close());

		// ESC closes
		scene.setOnKeyPressed( key -> {
		    if (key.getCode() == KeyCode.ESCAPE) {
		    	stage.close();
		    }
		});
		
		// The dialog is modal: the caller waits until it is gone.
		stage.showAndWait();
		centreOnScreen(stage);

		if (confirmation) {
			return Optional.of(confirmed[0] ? ButtonType.OK : ButtonType.CANCEL);
		}
		return Optional.of(ButtonType.OK);
	}

	/**
	 * The text of a "fine" dialog: parsed HTML in a text flow, plain text in a label that wraps.
	 *
	 * @param text the text, plain or HTML
	 * @return the node that shows the text
	 */
	static Region fineDialogText(String text) {
		if (text != null && isHtml(text)) {
			text = text.trim();
			text = text.replaceAll("(?i)^<html>|</html>$", "");
			final VBox lines = new VBox(4);
			for (final String line : text.split("(?i)<br\\s*/?>")) {
				lines.getChildren().add(htmlLine(line));
			}
			lines.setAlignment(Pos.CENTER);
			lines.getStyleClass().add("fine-dialog-text");
			return lines;
		}
		final Label label = new Label(text == null ? "" : text);
		label.setWrapText(true);
		label.setAlignment(Pos.CENTER);
			label.getStyleClass().add("fine-dialog-text");
		return label;
	}

	/**
	 * One line of HTML as a text flow: the tags it understands are bold, italic, underline and
	 * the colour of the font.
	 *
	 * @param html the line
	 * @return the text flow with the line
	 */
	static TextFlow htmlLine(String html) {
		final TextFlow flow = new TextFlow();
			// The text is centred like the plain text of the dialog.
		flow.setTextAlignment(TextAlignment.CENTER);
		flow.getStyleClass().add("fine-dialog-text");
		boolean bold = false;
		boolean italic = false;
		boolean underline = false;
		Color color = null;
		int start = 0;
		final Matcher matcher = HTML_TAG.matcher(html);
		while (matcher.find()) {
			if (matcher.start() > start) {
				flow.getChildren().add(htmlText(html.substring(start, matcher.start()), bold, italic,
						underline, color));
			}
			final String tag = matcher.group().toLowerCase();
			// The name of the tag: "<font color=\"#ff5555\">" is the tag "font".
			final Matcher name = HTML_TAG_NAME.matcher(tag);
			final String tagName = name.find() ? name.group(1) : "";
			if (tag.startsWith("</")) {
				switch (tagName) {
					case "b" -> bold = false;
					case "i" -> italic = false;
					case "u" -> underline = false;
					case "font" -> color = null;
					default -> { }
				}
			} else {
				switch (tagName) {
					case "b" -> bold = true;
					case "i" -> italic = true;
					case "u" -> underline = true;
					case "font" -> {
						final Matcher colour = HTML_COLOUR.matcher(tag);
						color = colour.find() ? Color.web(colour.group(1)) : null;
					}
					default -> { }
				}
			}
			start = matcher.end();
		}
		if (start < html.length()) {
			flow.getChildren().add(htmlText(html.substring(start), bold, italic, underline, color));
		}
		return flow;
	}

	/**
	 * A piece of text with its formatting.
	 *
	 * <p>Bold, italic and underline are style classes, so the font of the application is kept;
	 * the colour of a font tag is set directly.</p>
	 *
	 * @param text      the text
	 * @param bold      bold
	 * @param italic    italic
	 * @param underline underlined
	 * @param color     the colour, or {@code null} for the default one
	 * @return the text
	 */
	static Text htmlText(String text, boolean bold, boolean italic, boolean underline,
			Color color) {
		final Text node = new Text(unescape(text));
		// A text node does not inherit the fill colour of its parent: the colour is set here, the
		// colour of a font tag wins.
		node.setFill(color != null ? color : Color.web(DIALOG_TEXT_COLOR));
		if (bold) {
			node.getStyleClass().add("fine-dialog-bold");
		}
		if (italic) {
			node.getStyleClass().add("fine-dialog-italic");
		}
		if (underline) {
			node.getStyleClass().add("fine-dialog-underline");
		}
		return node;
	}

	/**
	 * Replaces the HTML entities of a text by their characters.
	 *
	 * @param text the text
	 * @return the text with the entities replaced
	 */
	private static String unescape(String text) {
		return text.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
				.replace("&#39;", "'").replace("&nbsp;", " ").replace("&amp;", "&");
	}

	/**
	 * Whether the text carries HTML markup: the subset the dialogs understand - bold, italic,
	 * underline, line breaks and colours.
	 *
	 * @param text the text
	 * @return {@code true} if the text is HTML
	 */
	static boolean isHtml(String text) {
		final String lower = text.toLowerCase();
		return lower.contains("<b>") || lower.contains("<i>") || lower.contains("<u>")
                || lower.contains("<br") || lower.contains("<font") || lower.contains("&lt;")
                || lower.contains("&nbsp;") || lower.contains("&amp;");
	}

	/**
	 * Centres a window on the screen.
	 *
	 * @param stage the window
	 */
	private static void centreOnScreen(Stage stage) {
		final Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
		stage.setX(bounds.getMinX() + (bounds.getWidth() - stage.getWidth()) / 2);
		stage.setY(bounds.getMinY() + (bounds.getHeight() - stage.getHeight()) / 2);
	}
	
	/**
	 * Show confirmation dialog.
	 * 
	 * @param app the application
	 * @param question the question
	 * @return chosen option
	 */
	public static Optional<ButtonType> showConfirmationDialog(BaseApp app, String question) {
		// https://code.makery.ch/blog/javafx-dialogs-official
		final Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.getDialogPane().getStylesheets().addAll(Helper.getApplicationStyleSheets(app));
		alert.setTitle(Helper.getTranslatedText(app, "dialog.Confirmation"));
		alert.setHeaderText(null);

		final Text confirmationGlyph = Icons.create("question", 32);
		if (confirmationGlyph != null) {
			confirmationGlyph.setStyle("-fx-fill: #ffffff;");
			final StackPane confirmationBox = new StackPane(confirmationGlyph);
			confirmationBox.setPrefSize(48, 48);
			confirmationBox.setMinSize(48, 48);
			confirmationBox.setMaxSize(48, 48);
			confirmationBox.setAlignment(Pos.CENTER);
			confirmationBox.setStyle("-fx-background-color: #0E4E74; -fx-background-radius: 24;");
			alert.setGraphic(confirmationBox);
		}

		final Label label = new Label(question);
		label.setWrapText(true);
		alert.getDialogPane().setContent(label);
		//alert.setContentText(question);
		
		return alert.showAndWait();
	}

	/**
	 * Show information dialog.
	 * 
	 * @param app the application
	 * @param text the information
	 */
	public static void showInformationDialog(BaseApp app, String text) {
		final Alert info = new Alert(AlertType.INFORMATION);
		info.getDialogPane().getStylesheets().addAll(Helper.getApplicationStyleSheets(app));
		info.setTitle(Helper.getTranslatedText(app, "dialog.Information"));
		info.setHeaderText(null);

		final Text informationGlyph = Icons.create("info", 32);
		if (informationGlyph != null) {
			informationGlyph.setStyle("-fx-fill: #ffffff;");
			final StackPane informationBox = new StackPane(informationGlyph);
			informationBox.setPrefSize(48, 48);
			informationBox.setMinSize(48, 48);
			informationBox.setMaxSize(48, 48);
			informationBox.setAlignment(Pos.CENTER);
			informationBox.setStyle("-fx-background-color: #0E4E74; -fx-background-radius: 24;");
			info.setGraphic(informationBox);
		}
		
		final Label label = new Label(text);
		label.setWrapText(true);
		info.getDialogPane().setContent(label);
		//info.setContentText(text);
		
		info.showAndWait();		
	}	

	/**
	 * Show warning dialog.
	 * 
	 * @param app the application
	 * @param text the warning
	 */
	public static void showWarningDialog(BaseApp app, String text) {
		UI.showWarningDialog(app, text, -1, -1);
	}
	
	/**
	 * Show warning dialog.
	 * 
	 * @param app the application
	 * @param text the warning
	 * @param width width
	 * @param height height
	 */
	public static void showWarningDialog(BaseApp app, String text, int width, int height) {
		final Alert info = new Alert(AlertType.WARNING);
		info.getDialogPane().getStylesheets().addAll(Helper.getApplicationStyleSheets(app));
		info.setTitle(Helper.getTranslatedText(app, "dialog.Warning"));
		final Text warningGlyph = Icons.create("warning", 32);
		if (warningGlyph != null) {
			warningGlyph.setStyle("-fx-fill: #ffffff;");
			final StackPane warningBox = new StackPane(warningGlyph);
			warningBox.setPrefSize(48, 48);
			warningBox.setMinSize(48, 48);
			warningBox.setMaxSize(48, 48);
			warningBox.setAlignment(Pos.CENTER);
			warningBox.setStyle("-fx-background-color: #885000; -fx-background-radius: 24;");
			info.setGraphic(warningBox);
		}
		info.setHeaderText(null);
		final Label label = new Label(text);
		label.setPadding(new Insets(12, 12, 12, 12));
		label.setWrapText(true);
		final StackPane stackPane = new StackPane(label);
        stackPane.setPrefSize(width == -1 ? 300 : width, height == -1 ? 100 : height); // Set preferred size
        stackPane.setAlignment(Pos.TOP_LEFT); // Center the label
        // Set the StackPane as the content of the dialog pane
		info.getDialogPane().setContent(stackPane);
		info.showAndWait();		
	}	
	
	/**
	 * Show exception dialog.
	 * 
	 * @param ex exception
	 */
	public static void showExceptionDialog(Throwable ex) {
	    final Alert alert = new Alert(Alert.AlertType.ERROR);
	    alert.getDialogPane().getStylesheets().add(Helper.getBaseStyle());

	    // Header: exception type + message (never empty)
	    String header = ex.getClass().getSimpleName()
	            + (ex.getLocalizedMessage() != null ? ": " + ex.getLocalizedMessage() : "");
	    alert.setHeaderText(header);

	    // Icon (safe)
	    try {
	        final Text errorGlyph = Icons.create("x-circle", 32);
	        if (errorGlyph != null) {
	            errorGlyph.setStyle("-fx-fill: #ffffff;");
	            final StackPane errorBox = new StackPane(errorGlyph);
	            errorBox.setPrefSize(48, 48);
	            errorBox.setMinSize(48, 48);
	            errorBox.setMaxSize(48, 48);
	            errorBox.setStyle("-fx-background-color: #aa0000; -fx-background-radius: 24;");
	            alert.setGraphic(errorBox);
	        }
	    } catch (Exception ignore) { /* don't let icon break the dialog */ }

	    // Content: full stack trace (includes cause chain)
	    final StringWriter sw = new StringWriter();
	    ex.printStackTrace(new PrintWriter(sw));

	    GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		
	    final TextArea area = new TextArea(sw.toString());
	    area.setEditable(false);
	    area.getStyleClass().add("exception-text-area");	    
	    area.setMaxWidth(Double.MAX_VALUE);
	    area.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(area, Priority.ALWAYS);
		GridPane.setHgrow(area, Priority.ALWAYS);	    
	    area.setWrapText(false);
	    area.setPrefRowCount(20);
	    area.setPrefColumnCount(100);
	    
		expContent.add(area, 0, 0);
	    
	    alert.getDialogPane().setContent(expContent);

	    alert.getDialogPane().setPrefWidth(800);
	    alert.getDialogPane().setPrefHeight(640);
	    alert.setResizable(true);

	    alert.showAndWait();
	}
	
	/**
	 * Show exception dialog.
	 * 
	 * @param ex exception
	 * @param reason reason
	 */
	public static void showExceptionDialogExtended(Throwable ex, String reason) {
		
    	if (UI.baseResourcePath == null)
			LOG.warn("UI: Base resource path for app has not been set; call UI.setup(...) to style panes per app!");
		
		Alert alert = new Alert(Alert.AlertType.ERROR);
		// Always the framework style sheet: this handler runs for every uncaught exception and
		// has no application reference, so it must work for any application (see
		// Helper.getApplicationStyleSheets for the dialogs that do have one).
		alert.getDialogPane().getStylesheets().add(Helper.getBaseStyle());
		alert.getDialogPane().getButtonTypes().forEach(buttonType ->
	    	alert.getDialogPane().lookupButton(buttonType).getStyleClass().add("error-alert-button")
		);		
		alert.setTitle(" Exception");
		alert.setHeaderText(reason);
		// Framework glyph instead of the default (light) alert icon.
		final Text exceptionGlyph = Icons.create("bug", 32);
		if (exceptionGlyph != null) {
			exceptionGlyph.setStyle("-fx-fill: #ffffff;");
			final StackPane exceptionBox = new StackPane(exceptionGlyph);
			exceptionBox.setPrefSize(48, 48);
			exceptionBox.setMinSize(48, 48);
			exceptionBox.setMaxSize(48, 48);
			exceptionBox.setAlignment(Pos.CENTER);
			exceptionBox.setStyle("-fx-background-color: #aa0000; -fx-background-radius: 24;");
			alert.setGraphic(exceptionBox);
		}

		alert.setContentText(ex.getLocalizedMessage());

		// Create expandable Exception.
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		ex.printStackTrace(pw);
		pw.flush();
		String exceptionText = sw.toString();

		Label label = new Label(Messages.t("dialog.Exception.was", baseResourcePath));

		TextArea textArea = new TextArea(exceptionText);
		textArea.setEditable(false);
		textArea.getStyleClass().add("exception-text-area");
		textArea.setMaxWidth(Double.MAX_VALUE);
		textArea.setMaxHeight(Double.MAX_VALUE);
		GridPane.setVgrow(textArea, Priority.ALWAYS);
		GridPane.setHgrow(textArea, Priority.ALWAYS);
		GridPane expContent = new GridPane();
		expContent.setMaxWidth(Double.MAX_VALUE);
		expContent.add(label, 0, 0);
		expContent.add(textArea, 0, 1);

		// Set expandable Exception into the dialog pane.
		alert.getDialogPane().setPrefWidth(600);
		alert.getDialogPane().setPrefHeight(400);
		alert.getDialogPane().setExpandableContent(expContent);
		alert.showAndWait();
	}
	
	/**
	 * Show error pane.
	 * 
	 * @param app the application
	 * @param ex exception
	 */
	public static void showError(BaseApp app, Throwable ex) {
    	UI.showError(app, ex, null);
	}
	
	/**
	 * Show error pane.
	 * 
	 * @param app the application
	 * @param ex exception
	 * @param title title or null
	 */
	public static void showError(BaseApp app, Throwable ex, String title) {
		title = title == null ? Helper.getTranslatedText(app, "Error.title") : title;
    	UI.showPane(
    			app, 
    			null,
    			new ErrorAction(ex),
    			Constants.VIEW_DEFAULT_INFO, 
    			title, 
    			"x-circle" 
    		);		
	}

	/**
	 * Show error pane.
	 * 
	 * @param app the application
	 * @param msg message
	 * @param title title or null
	 */
	public static void showError(BaseApp app, String msg, String title) {
		title = title == null ? Helper.getTranslatedText(app, "Error.title") : title;
    	UI.showPane(
    			app, 
    			null,
    			new ErrorAction(msg),
    			Constants.VIEW_DEFAULT_INFO, 
    			title, 
    			"x-circle" 
    		);		
	}
	
	/**
	 * Show error pane.
	 * 
	 * @param app the application
	 * @param ex exception
	 * @param title title or null
	 */
	public static void showErrorAndWait(BaseApp app, Throwable ex, String title) {
		title = title == null ? Helper.getTranslatedText(app, "Error.title") : title;
    	UI.showPaneAndWait(
    			app, 
    			null,
    			new ErrorAction(ex),
    			Constants.VIEW_DEFAULT_INFO, 
    			title, 
    			"x-circle" 
    		);		
	}
	
	/**
	 * Show error pane.
	 * 
	 * @param app the application
	 * @param msg message
	 * @param title title or null
	 */
	public static void showErrorAndWait(BaseApp app, String msg, String title) {
		title = title == null ? Helper.getTranslatedText(app, "Error.title") : title;
    	UI.showPaneAndWait(
    			app, 
    			null,
    			new ErrorAction(msg),
    			Constants.VIEW_DEFAULT_INFO, 
    			title, 
    			"x-circle" 
    		);		
	}
	
	/**
	 * {@code Configuration › JSON Formatter}: show the JSON formatter window.
	 *
	 * @param app the application
	 * @return the pane controller
	 */
	public static DefaultCodeEditorController showJsonFormatter(BaseApp app) {
		return UI.showJsonFormatter(app, null);
	}

	/**
	 * Show the JSON formatter window. The window is set up exactly like every other pane:
	 * the view is created with its standard constructor, {@link #setup} loads the FXML onto it
	 * (which also binds the controller) and title, icon, footer buttons and the note text are
	 * set on the controller <b>before</b> the stage is shown.
	 *
	 * @param app   the application
	 * @param title the window title, or {@code null} for the translated default
	 * @return the pane controller
	 */
	public static DefaultCodeEditorController showJsonFormatter(BaseApp app, String title) {
		title = title == null ? Helper.getTranslatedText(app, "JSONFormatter.title") : title;

		final Stage stage = new Stage();
		stage.setResizable(true);
		stage.setMinHeight(480);
		stage.setMinWidth(640);

		// JSON formatter view
		final JsonFormatterView view = new JsonFormatterView(app, stage, null);
		// Setup UI, must be made after the view!
		final DefaultCodeEditorController controller =
				(DefaultCodeEditorController) UI.setup(app, baseResourcePath, view, Constants.VIEW_CODE_EDITOR);
		// Customize
		controller.customize(title, "brackets-curly", null);

		// Transient 'copied to clipboard' note, fading out.
		final FadeTransition noteFade = new FadeTransition(Duration.millis(2550), controller.getHelpLabel());
		noteFade.setFromValue(1.0);
		noteFade.setToValue(0.0);
		noteFade.setOnFinished(_ -> {
			controller.setHelpText(null);
			controller.getHelpLabel().setOpacity(1.0);
		});

		final Button btnCopy = new Button(Helper.getTranslatedText(app, "JSONFormatter.button.copy"));
		btnCopy.setOnAction(_ -> {
			final ClipboardContent content = new ClipboardContent();
			content.putString(view.getText());
			Clipboard.getSystemClipboard().setContent(content);

			noteFade.stop();
			controller.getHelpLabel().setOpacity(1.0);
			controller.setHelpText(Helper.getTranslatedText(app, "JSONFormatter.note.copied"));
			noteFade.playFromStart();
		});

		final Button btnClear = new Button(Helper.getTranslatedText(app, "JSONFormatter.button.clear"));
		btnClear.setOnAction(_ -> {
			noteFade.stop();
			controller.getHelpLabel().setOpacity(1.0);
			controller.setHelpText(null);
			view.setText("");
		});

		final Button btnFormat = new Button(Helper.getTranslatedText(app, "JSONFormatter.button.format"));
		btnFormat.setOnAction(_ -> {
			if (!view.format())
				UI.showFineWarningDialog(app, Helper.getTranslatedText(app, "JSONFormatter.parse.error"));
		});

		// Additional buttons and the note text are set per setter before the stage is shown.
		controller.setButtons(List.of(btnCopy, btnClear, btnFormat));

		stage.setTitle(title);
		for (final URL url : Helper.getApplicationIcons(app))
			stage.getIcons().add(new Image(url.toExternalForm()));

		// Not modal: the formatter stays usable next to the main window.
		stage.initModality(Modality.NONE);
		// ... but owned by the main window: closing the main window closes this one too.
		if (app.getPrimaryStage() != null && app.getPrimaryStage() != stage)
			stage.initOwner(app.getPrimaryStage());

		// Show stage
		stage.show();
		stage.centerOnScreen();

		return controller;
	}

	/**
	 * Show about pane.
	 * 
	 * @param app the application
	 */
	public static void showAbout(BaseApp app) {
		UI.showAbout(app, "about");
	}

	/**
	 * Show about pane.
	 * 
	 * @param app the application
	 * @param template template name (HTML file name without extension)
	 */
	public static void showAbout(BaseApp app, String template) {
		UI.showAbout(app, template, null);
	}

	/**
	 * Show about pane.
	 * 
	 * @param app the application
	 * @param template template name (HTML file name without extension)
	 * @param title title or null
	 */
	public static void showAbout(BaseApp app, String template, String title) {
		title = title == null ? Helper.getTranslatedText(app, "About.title") : title;
    	UI.showPane(
    			app, 
    			null,
    			new HTMLAction(template, null),
    			Constants.VIEW_DEFAULT_INFO, 
    			title 
    		);		
	}

	/**
	 * Show about pane with license button.
	 * 
	 * @param app the application
	 */
	public static void showAboutWithLicense(BaseApp app) {
		UI.showAboutWithLicense(app, "info");
	}

	/**
	 * Show about pane with license button.
	 * 
	 * @param app the application
	 * @param iconFile icon file name (a resource in autumo-ui library) or absolute icon file name path
	 */
	public static void showAboutWithLicense(BaseApp app, String iconFile) {
    	UI.showPane(
    			app, 
    			null,
    			new AboutWithLicenseAction(),
    			Constants.VIEW_DEFAULT_INFO, 
    			Helper.getTranslatedText(app, "About.title"),
				iconFile
    		);		
	}
	
	/**
	 * Show HTML pane.
	 * 
	 * @param app the application
	 * @param template template name (HTML file name without extension)
	 * @param title translated title (must)
	 */
	public static void showHTML(BaseApp app, String template, String title) {
    	UI.showPane(
    			app, 
    			null,
    			new HTMLAction(template, null),
    			Constants.VIEW_DEFAULT_INFO, 
    			title 
    		);		
	}
	
	/**
	 * Show License.
	 * 
	 * @param app the application
	 */
	public static void showLicense(BaseApp app) {
    	UI.showPane(
    			app, 
    			null,
    			new HTMLAction("license", null),
    			Constants.VIEW_DEFAULT_INFO, 
    			Helper.getTranslatedText(app, "License.title"),
    			"autumo48.png" // autumo-ui-lib internal
    		);
	}

	/**
	 * Show a default pane in an own stage.
	 * 
	 * @param app the application used, if not already set by the action itself 
	 * @param action the action to execute on the pane
	 * @param title title
	 * @return controller view controller
	 */
	public static DefaultInfoController showPane(BaseApp app, Action action, String title) {
		return UI.showPane(app, null, action, Constants.VIEW_DEFAULT_INFO, title, "info", null);
	}
	
	/**
	 * Show a default pane in an own stage.
	 * 
	 * @param app the application used, if not already set by the action itself 
	 * @param stage the stage or null (an new one will be created)
	 * @param action the action to execute on the pane
	 * @param title title
	 * @return controller view controller
	 */
	public static DefaultInfoController showPane(BaseApp app, Stage stage, Action action, String title) {
		return UI.showPane(app, stage, action, Constants.VIEW_DEFAULT_INFO, title, "info", null);
	}

	/**
	 * Show a default pane in an own stage.
	 * 
	 * @param app the application used, if not already set by the action itself 
	 * @param stage the stage or null (an new one will be created)
	 * @param action the action to execute on the pane
	 * @param title title
	 * @return controller view controller
	 */
	public static DefaultInfoController showPaneAndWait(BaseApp app, Stage stage, Action action, String title) {
		return UI.showPaneAndWait(app, stage, action, Constants.VIEW_DEFAULT_INFO, title, "info", null);
	}
	
	/**
	 * Show a default pane in an own stage.
	 * 
	 * @param app the application used, if not already set by the action itself 
	 * @param stage the stage or null (an new one will be created)
	 * @param action the action to execute on the pane
	 * @param fxmlName FXML view file name
	 * @param title title
	 * @return controller view controller
	 */
	public static DefaultInfoController showPane(BaseApp app, Stage stage, Action action, String fxmlName, String title) {
		return UI.showPane(app, stage, action, fxmlName, title, "info", null);
	}
	
	/**
	 * Show a default pane in an own stage.
	 * 
	 * @param app the application used, if not already set by the action itself 
	 * @param stage the stage or null (an new one will be created)
	 * @param action the action to execute on the pane
	 * @param fxmlName FXML view file name
	 * @param title title
	 * @param iconFile icon file name (a resource in autumo-ui library)
	 * @return controller view controller
	 */
	public static DefaultInfoController showPane(BaseApp app, Stage stage, Action action, String fxmlName, String title, String iconFile) {
		return UI.showPane(app, stage, action, fxmlName, title, iconFile, null);
	}
	
	/**
	 * Show a default pane in an own stage.
	 * 
	 * @param app the application used, if not already set by the action itself 
	 * @param stage the stage or null (an new one will be created)
	 * @param action the action to execute on the pane
	 * @param fxmlName FXML view file name
	 * @param title title
	 * @param iconFile icon file name (a resource in autumo-ui library)
	 * @return controller view controller
	 */
	public static DefaultInfoController showPaneAndWait(BaseApp app, Stage stage, Action action, String fxmlName, String title, String iconFile) {
		return UI.showPaneAndWait(app, stage, action, fxmlName, title, iconFile, null);
	}
	
	/**
	 * Show a default pane in an own stage.
	 * 
	 * @param app the application used, if not already set by the action itself 
	 * @param stage the stage or null (an new one will be created)
	 * @param action the action to execute on the pane
	 * @param fxmlName FXML view file name
	 * @param title title
	 * @param iconFile icon file name (a resource in autumo-ui library)
	 * @param footerText footer text or null
	 * @return controller view controller
	 */
	public static DefaultInfoController showPane(BaseApp app, Stage stage, Action action, String fxmlName, String title, String iconFile, String footerText) {
		return UI.showPane(app, stage, action, fxmlName, title, iconFile, footerText, null);
	}
	
	/**
	 * Show a default pane in an own stage.
	 * 
	 * @param app the application used, if not already set by the action itself 
	 * @param stage the stage or null (an new one will be created)
	 * @param action the action to execute on the pane
	 * @param fxmlName FXML view file name
	 * @param title title
	 * @param iconFile icon file name (a resource in autumo-ui library)
	 * @param footerText footer text or null
	 * @return controller view controller
	 */
	public static DefaultInfoController showPaneAndWait(BaseApp app, Stage stage, Action action, String fxmlName, String title, String iconFile, String footerText) {
		return UI.showPaneAndWait(app, stage, action, fxmlName, title, iconFile, footerText, null);
	}
	
	/**
	 * Show a default pane in an own stage.
	 * 
	 * @param app the application used, if not already set by the action itself 
	 * @param stage the stage or null (an new one will be created)
	 * @param action the action to execute on the pane
	 * @param fxmlName FXML view file name
	 * @param title title
	 * @param iconFile icon file name (a resource in autumo-ui library) or absolute icon file name path to resource
	 * @param footerText footer text or null
	 * @param styleResourcePath relative style ('style/style.css') file name or fully qualified resource path
	 * @return controller view controller
	 */
	public static DefaultInfoController showPane(BaseApp app, Stage stage, Action action, String fxmlName, String title, String iconFile, String footerText, String styleResourcePath) {
		
		if (stage == null) {
	    	stage = new Stage();
	    	stage.setResizable(true);
	    	// Panes must be resizable down to the framework minimum 640x480.
	    	stage.setMinHeight(480);
	    	stage.setMinWidth(640);			
		}
		
		// All secondary panes are owned by the main window, so they close together with it
		if (app.getPrimaryStage() != null && app.getPrimaryStage() != stage)
			stage.initOwner(app.getPrimaryStage());
		
        // Default Info view
        final DefaultInfoView defView = new DefaultInfoView(app, stage, styleResourcePath);
        // Setup UI, must be made after the view!
        final DefaultInfoController controller = (DefaultInfoController) UI.setup(app, baseResourcePath, defView, fxmlName);
        // Customize
        controller.customize(title, iconFile, footerText);
        
        if (action instanceof CustomSizeAction) {
        	stage.setWidth(((CustomSizeAction)action).getWidth());
        	stage.setHeight(((CustomSizeAction)action).getHeight());
        }
        
    	stage.initModality(action.getStageModality());	
        
        // Show stage
    	stage.show();
    	stage.centerOnScreen();

    	// Only set the app if it is not already hold by the action!
    	if (action.getApp() == null)
    		action.setApp(app);

    	action.setController(controller);
    	
    	// Execute the action
    	action.run();
    	
    	return controller;
	}

	/**
	 * Show a default pane in an own stage.
	 * 
	 * @param app the application used, if not already set by the action itself 
	 * @param stage the stage or null (an new one will be created)
	 * @param action the action to execute on the pane
	 * @param fxmlName FXML view file name
	 * @param title title
	 * @param iconFile icon file name (a resource in autumo-ui library) or absolute icon file name path to resource
	 * @param footerText footer text or null
	 * @param styleResourcePath relative style ('style/style.css') file name or fully qualified resource path
	 * @return controller view controller
	 */
	public static DefaultInfoController showPaneAndWait(BaseApp app, Stage stage, Action action, String fxmlName, String title, String iconFile, String footerText, String styleResourcePath) {
		
		if (stage == null) {
	    	stage = new Stage();
	    	stage.setResizable(true);
	    	// Panes must be resizable down to the framework minimum 640x480.
	    	stage.setMinHeight(480);
	    	stage.setMinWidth(640);			
		}
		
		// All secondary panes are owned by the main window, so they close together with it
		if (app.getPrimaryStage() != null && app.getPrimaryStage() != stage)
			stage.initOwner(app.getPrimaryStage());
		
        // Default Info view
        final DefaultInfoView defView = new DefaultInfoView(app, stage, styleResourcePath);
        // Setup UI, must be made after the view!
        final DefaultInfoController controller = (DefaultInfoController) UI.setup(app, baseResourcePath, defView, fxmlName);
        // Customize
        controller.customize(title, iconFile, footerText);
        
        if (action instanceof CustomSizeAction) {
        	stage.setWidth(((CustomSizeAction)action).getWidth());
        	stage.setHeight(((CustomSizeAction)action).getHeight());
        }
        
    	stage.initModality(action.getStageModality());	

    	// Only set the app if it is not already hold by the action!
    	if (action.getApp() == null)
    		action.setApp(app);

    	action.setController(controller);
    	
    	// Execute the action
    	action.run();

        // Show stage
    	stage.showAndWait();
    	stage.centerOnScreen();
    	
    	return controller;
	}
	
	/**
	 * Show registration in an own stage.
	 * 
	 * @param app the licensed application used, in most cases the same as <code>app</code> 
	 * @return controller view controller
	 */
	public static RegistrationController showRegistrationPane(BaseApp app) {
		return UI.showRegistrationPane(app, app, Helper.getTranslatedText(app, "Registration.title"));
	}
	
	/**
	 * Show registration in an own stage.
	 * 
	 * @param app the application used 
	 * @param licApp the licensed application used, in most cases the same as <code>app</code> 
	 * @param title title
	 * @return controller view controller
	 */
	public static RegistrationController showRegistrationPane(BaseApp app, LicensedApplication licApp, String title) {
		
		final Stage stage = new Stage();
    	stage.setResizable(false);
    	stage.setMinHeight(628);
    	stage.setMinWidth(800);			
		
        // Registration view
        final RegistrationView regView = new RegistrationView(app, stage, null);
        // Setup UI, must be made after the view!
        final RegistrationController controller = (RegistrationController) UI.setup(app, baseResourcePath, regView, Constants.VIEW_REGISTRATION);
        // Customize
        if (title == null)
        	title = Helper.getTranslatedText(app, "Registration.title");
        controller.customize(title, "seal-check", "");
        
        final RegistrationAction action = new RegistrationAction(licApp);
        stage.initModality(action.getStageModality());
        
        // Show stage
    	stage.show();
    	stage.centerOnScreen();

    	// Only set the app if it is not already hold by the action!
    	if (action.getApp() == null)
    		action.setApp(app);

    	action.setController(controller);
    	
    	// Execute the action
    	action.run();
    	
    	return controller;
	}

	/**
	 * Show remove pane in an own stage.
	 * 
	 * @param app the application used 
	 * @return controller view controller
	 */
	public static DefaultInfoController showRemovePane(BaseApp app) {
		return UI.showRemovePane(app, Helper.getTranslatedText(app, "Remove.title"));
	}
	
	/**
	 * Show remove pane in an own stage.
	 * 
	 * @param app the application used 
	 * @param title title
	 * @return controller view controller
	 */
	public static DefaultInfoController showRemovePane(BaseApp app, String title) {
    	return UI.showPane(
    			app, 
    			null,
    			new RemoveAction(),
    			Constants.VIEW_DEFAULT_INFO, 
    			title, 
    			"trash" 
    		);    
	}
	
	/**
	 * Hyperlink listener for HTML dialog views.
	 */
	public static class HyperLinkRedirectListener implements ChangeListener<Worker.State>, EventListener {
		
	    private static final String CLICK_EVENT = "click";
	    private static final String ANCHOR_TAG = "a";

	    private final WebView webView;

	    /**
	     * Hyperlink listener.
	     * @param webView web view
	     */
	    public HyperLinkRedirectListener(WebView webView) {
	        this.webView = webView;
	    }

	    @Override
	    public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
	        if (Worker.State.SUCCEEDED.equals(newValue)) {
	            Document document = webView.getEngine().getDocument();
	            NodeList anchors = document.getElementsByTagName(ANCHOR_TAG);
	            for (int i = 0; i < anchors.getLength(); i++) {
	                Node node = anchors.item(i);
	                EventTarget eventTarget = (EventTarget) node;
	                eventTarget.addEventListener(CLICK_EVENT, this, false);
	            }
	        }
	    }

	    @Override
	    public void handleEvent(Event event) {
	        //HTMLAnchorElement anchorElement = (HTMLAnchorElement) event.getCurrentTarget();
	        String href = event.getCurrentTarget().toString(); //anchorElement.getHref();
	        if (Desktop.isDesktopSupported()) {
	            openLinkInSystemBrowser(href);
	        } else {
	        }
	        event.preventDefault();
	    }

	    private void openLinkInSystemBrowser(String url) {
	        try {
	            URI uri = new URI(url);
	            Desktop.getDesktop().browse(uri);
	        } catch (Throwable e) {
	        }
	    }
	}
	
}
