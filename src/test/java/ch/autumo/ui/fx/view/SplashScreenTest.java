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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Tests of the splash screen.
 *
 * <p>The splash screen is a window of its own: the test shows it, checks its parts and hides it
 * again. Without a display (a build server) the test is skipped.</p>
 */
class SplashScreenTest {

    /** The splash screen of the test. */
    private static SplashScreen splash;

    /**
     * Starts the JavaFX toolkit and creates the splash screen.
     *
     * @throws Exception if the toolkit does not start
     */
    @BeforeAll
    static void buildSplash() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(),
                "the splash screen needs a display");
        FxToolkit.start();
        splash = new SplashScreen();
    }

    /**
     * Closes the JavaFX toolkit.
     */
    @AfterAll
    static void closeSplash() {
        if (splash != null) {
            splash.hide();
        }
    }

    /**
     * The splash screen shows the logo, the scanner below it and the standard colours; it goes
     * away with {@link SplashScreen#hide()}.
     */
    @Test
    void theSplashScreenShowsTheLogoAndTheScanner() {
        call(() -> {
            assertFalse(splash.isShowing(), "the splash screen is not up yet");
            splash.show();
            assertTrue(splash.isShowing(), "the splash screen is up");

            final Stage stage = stage();
            assertNotNull(stage.getScene(), "the splash screen has a scene");
            assertTrue(stage.isAlwaysOnTop(), "the splash screen stays in front of the main window");
            final VBox frame = (VBox) stage.getScene().getRoot();
            final VBox box = (VBox) frame.getChildren().get(0);
            // The logo and the scanner.
            assertEquals(2, box.getChildren().size(),
                    "the splash screen shows the logo and the scanner");
            assertTrue(frame.getBackground().getFills().get(0).getFill() instanceof Color,
                    "the window has a background colour");
            assertEquals(SplashScreen.DEFAULT_BACKGROUND,
                    frame.getBackground().getFills().get(0).getFill(),
                    "the background is the one of the standard design");
            assertEquals(SplashScreen.DEFAULT_FRAME_COLOR,
                    frame.getBorder().getStrokes().get(0).getTopStroke(),
                    "the frame is the one of the standard design");

            splash.hide();
            assertFalse(splash.isShowing(), "the splash screen is gone");
            return null;
        });
    }

    /**
     * The colours, the radius and the logo are configurable.
     */
    @Test
    void theSplashScreenIsConfigurable() {        call(() -> {
            final SplashScreen own = new SplashScreen()
                    .setBackgroundColor(Color.web("#123456"))
                    .setFrameColor(Color.web("#654321"))
                    .setCornerRadius(4)
                    .setLogo((javafx.scene.image.Image) null);
            own.show();
            final VBox frame = (VBox) stage(own).getScene().getRoot();
            assertEquals(Color.web("#123456"), frame.getBackground().getFills().get(0).getFill(),
                    "the background is the one of the application");
            assertEquals(Color.web("#654321"), frame.getBorder().getStrokes().get(0).getTopStroke(),
                    "the frame is the one of the application");
            own.hide();
            return null;
        });
    }

    /**
     * Without a duration the splash screen goes away with the main window; with a duration it
     * stays at least that long.
     */
    @Test
    void theSplashScreenHasAMinimumDisplayTime() {
        call(() -> {
            final SplashScreen plain = new SplashScreen();
            assertNull(plain.getDuration(), "without a duration the splash goes with the main window");
            assertEquals(Duration.ZERO, plain.remainingDuration(),
                    "without a duration there is no time left to wait for");

            final SplashScreen timed = new SplashScreen().setDuration(Duration.seconds(30));
            assertEquals(Duration.seconds(30), timed.getDuration(), "the duration is set");
            timed.show();
            try {
                final Duration remaining = timed.remainingDuration();
                assertTrue(remaining.greaterThan(Duration.seconds(29)),
                        "the splash screen still has to stay up: " + remaining);
                assertTrue(remaining.lessThanOrEqualTo(Duration.seconds(30)),
                        "the splash screen does not stay longer than its duration: " + remaining);
            } finally {
                timed.hide();
            }
            return null;
        });
    }

    /** The window of the splash screen of the test. */
    private static Stage stage() {
        return stage(splash);
    }

    /** The window of a splash screen. */
    private static Stage stage(SplashScreen screen) {
        try {
            final java.lang.reflect.Field field = SplashScreen.class.getDeclaredField("stage");
            field.setAccessible(true);
            return (Stage) field.get(screen);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Runs the action on the JavaFX thread. */
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
