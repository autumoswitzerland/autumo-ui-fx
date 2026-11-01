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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javafx.application.Platform;

/**
 * The JavaFX toolkit of the tests: it is started once for the whole test run and stays up, so
 * the test classes can share it.
 */
public final class FxToolkit {

    /** Whether the toolkit was started. */
    private static boolean started = false;

    /** Starts the JavaFX toolkit, if it is not up already. */
    public static synchronized void start() {
        if (started) {
            return;
        }
        started = true;
        final CountDownLatch latch = new CountDownLatch(1);
        Platform.startup(() -> {
            Platform.setImplicitExit(false);
            latch.countDown();
        });
        try {
            latch.await(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
