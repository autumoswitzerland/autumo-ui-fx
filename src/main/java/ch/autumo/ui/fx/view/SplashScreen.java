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

import java.io.InputStream;
import java.net.URL;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;

import ch.autumo.ui.Constants;
import javafx.animation.Animation;
import javafx.application.Platform;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * The splash screen an application shows while it starts.
 *
 * <p>The screen is a small, transparent window with the application's logo and a scanner bar
 * below it: a light that sweeps from left to right and back, like the one of the reference
 * design. The window has rounded corners and a thin frame; background, frame colour, corner
 * radius, logo and an optional jingle are configurable, everything else follows the standard
 * design of the framework.</p>
 *
 * <p>An application activates it in {@code BaseApp}:</p>
 *
 * <pre>
 * &#64;Override
 * protected boolean isSplashScreenEnabled() {
 *     return true;
 * }
 *
 * &#64;Override
 * protected SplashScreen createSplashScreen() {
 *     return new SplashScreen()
 *             .setLogo(getClass().getResource("icons/app-256.png"))
 *             .setJingle(getClass().getResource("sounds/start.wav"));
 * }
 * </pre>
 *
 * <p>The framework shows the screen at the beginning of the start-up and hides it as soon as the
 * main window is up. Without the activation ({@code isSplashScreenEnabled()} is {@code false} by
 * default) no splash screen is shown at all.</p>
 *
 * <p>The jingle is played with the Java Sound API, so it has to be a sound file the JDK can play
 * (WAV, AU or AIFF) - the application does not need the JavaFX media module for it.</p>
 */
public class SplashScreen {

    /** Background of the splash window (standard design). */
    public static final Color DEFAULT_BACKGROUND = Color.web("#0b0f14");

    /** Colour of the frame around the splash window (standard design). */
    public static final Color DEFAULT_FRAME_COLOR = Color.web("#1e2833");

    /** Radius of the rounded corners of the splash window (standard design). */
    public static final double DEFAULT_CORNER_RADIUS = 24;

    /** Colour of the scanner track (standard design). */
    public static final Color DEFAULT_SCANNER_TRACK = Color.web("#10161d");

    /** Colour of the scanner light at its start (standard design). */
    public static final Color DEFAULT_SCANNER_START = Color.web("#5aa9e6");

    /** Colour of the scanner light at its end (standard design). */
    public static final Color DEFAULT_SCANNER_END = Color.web("#9fd0f5");

    /** Edge length of the logo. */
    public static final double LOGO_SIZE = 220;

    /** Width of the scanner bar. */
    public static final double SCANNER_WIDTH = 240;

    /** Height of the scanner bar. */
    public static final double SCANNER_HEIGHT = 8;

    /** Width of the scanning light. */
    public static final double SCANNER_LIGHT_WIDTH = 60;

    /** Duration of one sweep of the scanner light. */
    public static final Duration SCAN_DURATION = Duration.seconds(2.4);

    /** Duration of the fade out when the splash screen goes away. */
    private static final Duration FADE_DURATION = Duration.millis(250);

    /** Padding around the logo and the scanner. */
    private static final double PADDING = 48;

    /** Distance between the logo and the scanner. */
    private static final double GAP = 28;

    /** The logo of the application. */
    private Image logo;

    /** Background of the splash window. */
    private Color background = DEFAULT_BACKGROUND;

    /** Colour of the frame around the splash window. */
    private Color frameColor = DEFAULT_FRAME_COLOR;

    /** Radius of the rounded corners. */
    private double cornerRadius = DEFAULT_CORNER_RADIUS;

    /** The jingle played when the splash screen appears, or {@code null}. */
    private URL jingle;

    /** Whether the jingle was already played. */
    private boolean jinglePlayed;

    /**
     * The minimum time the splash screen stays up, or {@code null} if it goes away with the main
     * window.
     */
    private Duration duration;

    /** The moment the splash screen was shown. */
    private long shownAt;

    /** The window of the splash screen; {@code null} while it is not shown. */
    private Stage stage;

    /** The scanner bar of the splash screen. */
    private Scanner scanner;

    /**
     * Creates a splash screen with the standard design and the framework's logo.
     */
    public SplashScreen() {
        this.logo = loadImage(Constants.class.getResource(
                Constants.BASE_RESOURCE_PATH + Constants.ICON_APP_DEFAULT_PREFIX + "256.png"));
    }

    /**
     * Sets the logo of the application.
     *
     * @param logo the logo, or {@code null} for no logo
     * @return this splash screen
     */
    public SplashScreen setLogo(Image logo) {
        this.logo = logo;
        return this;
    }

    /**
     * Sets the logo of the application from a resource.
     *
     * @param logo the resource of the logo, or {@code null} for no logo
     * @return this splash screen
     */
    public SplashScreen setLogo(URL logo) {
        this.logo = loadImage(logo);
        return this;
    }

    /**
     * Sets the background colour of the splash window.
     *
     * @param background the colour
     * @return this splash screen
     */
    public SplashScreen setBackgroundColor(Color background) {
        this.background = background;
        return this;
    }

    /**
     * Sets the colour of the frame around the splash window.
     *
     * @param frameColor the colour
     * @return this splash screen
     */
    public SplashScreen setFrameColor(Color frameColor) {
        this.frameColor = frameColor;
        return this;
    }

    /**
     * Sets the radius of the rounded corners of the splash window.
     *
     * @param cornerRadius the radius in pixels
     * @return this splash screen
     */
    public SplashScreen setCornerRadius(double cornerRadius) {
        this.cornerRadius = cornerRadius;
        return this;
    }

    /**
     * Sets the jingle that is played when the splash screen appears.
     *
     * <p>Setting the jingle while the splash screen is already up plays it right away - the
     * framework does it that way, because the application decides with its configuration whether
     * there is a jingle at all, and the configuration is read while the splash screen is
     * running.</p>
     *
     * @param jingle the resource of the sound file, or {@code null} for no jingle
     * @return this splash screen
     */
    public SplashScreen setJingle(URL jingle) {
        this.jingle = jingle;
        if (this.stage != null && !this.jinglePlayed) {
            this.jinglePlayed = true;
            playJingle();
        }
        return this;
    }
    /**
     * Sets the minimum time the splash screen stays up.
     *
     * <p>Without a duration the splash screen goes away as soon as the main window is up. With
     * a duration it stays at least that long - a short start-up would otherwise flash the
     * splash screen up for a fraction of a second only.</p>
     *
     * @param duration the minimum display time, or {@code null} for none
     * @return this splash screen
     */
    public SplashScreen setDuration(Duration duration) {
        this.duration = duration;
        return this;
    }

    /**
     * The minimum time the splash screen stays up, or {@code null} if it goes away with the main
     * window.
     *
     * @return the minimum display time or {@code null}
     */
    public Duration getDuration() {
        return this.duration;
    }

    /**
     * The time the splash screen still has to stay up.
     *
     * @return the remaining time, zero if there is no duration or it is over
     */
    public Duration remainingDuration() {
        if (this.duration == null || this.shownAt == 0) {
            return Duration.ZERO;
        }
        final Duration elapsed = Duration.millis(System.currentTimeMillis() - this.shownAt);
        final Duration remaining = this.duration.subtract(elapsed);
        return remaining.lessThan(Duration.ZERO) ? Duration.ZERO : remaining;
    }
    /**
     * Shows the splash screen.
     *
     * <p>The window appears centred on the screen, the scanner starts and the jingle is
     * played. Calling the method again while the splash screen is up does nothing.</p>
     */
    public void show() {
        if (this.stage != null) {
            return;
        }
        final VBox box = new VBox(GAP);
        box.setAlignment(Pos.CENTER);
        if (this.logo != null) {
            final ImageView image = new ImageView(this.logo);
            image.setFitWidth(LOGO_SIZE);
            image.setFitHeight(LOGO_SIZE);
            image.setPreserveRatio(true);
            image.setSmooth(true);
            box.getChildren().add(image);
        }
        this.scanner = new Scanner();
        box.getChildren().add(this.scanner);

        // The rounded window: the content with the background colour, the frame and the radius.
        final VBox frame = new VBox(box);
        frame.setAlignment(Pos.CENTER);
        frame.setBackground(new Background(new BackgroundFill(
                this.background, new CornerRadii(this.cornerRadius), new Insets(0))));
        frame.setBorder(new Border(new BorderStroke(
                this.frameColor,
                BorderStrokeStyle.SOLID,
                new CornerRadii(this.cornerRadius),
                BorderStroke.THIN)));
        frame.setPadding(new Insets(PADDING));

        final Stage splash = new Stage();
        splash.initStyle(StageStyle.TRANSPARENT);
        splash.initModality(Modality.NONE);
        splash.setTitle("");
        // The splash screen is a window, but it must not end the application when it goes away:
        // closing the last window would quit the application (the JSON formatter behaves the
        // same way - closing its window does not quit the application). An application that has
        // a splash screen therefore ends itself (Platform.exit()).
        Platform.setImplicitExit(false);
        // The splash screen is always on top: the main window comes up while it is still there
        // (the application decides how long it stays), and it has to stay in front of it.
        splash.setAlwaysOnTop(true);
        final Scene scene = new Scene(frame);
        scene.setFill(Color.TRANSPARENT);
        splash.setScene(scene);
        this.stage = splash;

        splash.show();
        centreOnScreen(splash);
        this.shownAt = System.currentTimeMillis();
        this.scanner.start();
        // The jingle starts with the splash screen - in its own thread, so the JavaFX thread does
        // not wait for the audio device. JavaFX has no event for "the window has been painted",
        // so the sound starts right before the first picture.
        if (!this.jinglePlayed) {
            this.jinglePlayed = true;
            playJingle();
        }
    }

    /**
     * Hides the splash screen: the window fades out and closes.
     *
     * <p>The window is closed in any case - the fade is a nice touch, but it only runs while the
     * JavaFX thread is free. A splash screen that stays open would keep the application
     * alive, so a second timer closes the window in any case.</p>
     */
    public void hide() {
        final Stage splash = this.stage;
        if (splash == null) {
            return;
        }
        this.stage = null;
        if (this.scanner != null) {
            this.scanner.stop();
            this.scanner = null;
        }
        final FadeTransition fade = new FadeTransition(FADE_DURATION, splash.getScene().getRoot());
        fade.setFromValue(1.0);
        fade.setToValue(0.0);
        fade.setOnFinished(_ -> splash.close());
        fade.play();
        // The backup: the window is closed even if the fade never finishes (the JavaFX thread
        // was busy when the fade started).
        final Timeline close = new Timeline(new KeyFrame(FADE_DURATION.add(Duration.millis(150)),
                _ -> splash.close()));
        close.play();
    }

    /**
     * Whether the splash screen is currently shown.
     * 
     * @return true if splash screen is visible
     */
    public boolean isShowing() {
        return this.stage != null;
    }

    /** Centres the splash window on the screen. */
    private static void centreOnScreen(Stage splash) {
        final Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        splash.setX(bounds.getMinX() + (bounds.getWidth() - splash.getWidth()) / 2);
        splash.setY(bounds.getMinY() + (bounds.getHeight() - splash.getHeight()) / 2);
    }

    /**
     * Plays the jingle, if one is defined.
     *
     * <p>The jingle starts in its own thread: opening the audio device takes a moment and it
     * goes through the AWT - the JavaFX thread must not wait for it, it paints the splash
     * screen.</p>
     */
    private void playJingle() {
        final URL sound = this.jingle;
        if (sound == null) {
            return;
        }
        final Thread thread = new Thread(() -> {
            try {
                final AudioInputStream in = AudioSystem.getAudioInputStream(sound);
                final Clip clip = AudioSystem.getClip();
                clip.open(in);
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                        try {
                            in.close();
                        } catch (Exception e) {
                            // nothing to do
                        }
                    }
                });
                clip.start();
            } catch (Exception e) {
                System.err.println("autumo UI: The jingle could not be played: " + e.getMessage());
            }
        }, "autumo-splash-jingle");
        thread.setDaemon(true);
        thread.start();
    }

    /** Loads an image from a resource; a broken resource gives no logo instead of an error. */
    private static Image loadImage(URL url) {
        if (url == null) {
            return null;
        }
        try (InputStream in = url.openStream()) {
            return new Image(in);
        } catch (Exception e) {
            System.err.println("autumo UI: The splash screen logo could not be loaded: " + e.getMessage());
            return null;
        }
    }

    /**
     * The scanner bar: a light that sweeps from left to right and back.
     *
     * <p>It is a {@link Pane} and not a {@link StackPane}: a StackPane lays its children out
     * (it centres them), and the position of the scanning light - which the animation moves -
     * would be overwritten by the layout on every pulse. A Pane leaves the children where they
     * are.</p>
     */
    private static final class Scanner extends Pane {

        /** The scanning light. */
        private final Rectangle light;

        /** The animation of the scanning light. */
        private final Timeline animation;

        /** Creates the scanner bar. */
        Scanner() {
            final Rectangle track = new Rectangle(SCANNER_WIDTH, SCANNER_HEIGHT);
            track.setArcWidth(SCANNER_HEIGHT);
            track.setArcHeight(SCANNER_HEIGHT);
            track.setFill(DEFAULT_SCANNER_TRACK);

            this.light = new Rectangle(SCANNER_LIGHT_WIDTH, SCANNER_HEIGHT);
            this.light.setArcWidth(SCANNER_HEIGHT);
            this.light.setArcHeight(SCANNER_HEIGHT);
            this.light.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, DEFAULT_SCANNER_START), new Stop(1, DEFAULT_SCANNER_END)));
            this.light.setEffect(new DropShadow(12, DEFAULT_SCANNER_START));
            this.light.setLayoutX(0);
            this.light.setLayoutY(0);

            getChildren().addAll(track, this.light);
            setPrefSize(SCANNER_WIDTH, SCANNER_HEIGHT);
            setMinSize(SCANNER_WIDTH, SCANNER_HEIGHT);
            setMaxSize(SCANNER_WIDTH, SCANNER_HEIGHT);

            final double right = SCANNER_WIDTH - SCANNER_LIGHT_WIDTH;
            this.animation = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(this.light.xProperty(), 0.0, Interpolator.EASE_BOTH)),
                    new KeyFrame(SCAN_DURATION.divide(2),
                            new KeyValue(this.light.xProperty(), right, Interpolator.EASE_BOTH)),
                    new KeyFrame(SCAN_DURATION,
                            new KeyValue(this.light.xProperty(), 0.0, Interpolator.EASE_BOTH)));
            this.animation.setCycleCount(Animation.INDEFINITE);
        }

        /** Starts the sweep. */
        void start() {
            this.animation.play();
        }

        /** Stops the sweep. */
        void stop() {
            this.animation.stop();
        }
    }
}
