package org.example.managers;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CapsManager {
    private static final Logger logger = Logger.getLogger(CapsManager.class.getName());
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "Caps-Manager");
        thread.setDaemon(true);
        return thread;
    });

    public static void forceOffDelayed() {
        scheduler.schedule(() -> {
            try {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                boolean capsEnabled = toolkit.getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
                logger.info("Checking caps state: " + capsEnabled);
                if (capsEnabled) {
                    logger.info("Caps lock is enabled. Try to disable");
                    try {
                        toolkit.setLockingKeyState(KeyEvent.VK_CAPS_LOCK, false);
                        logger.info("Caps lock disabled. Current caps state - "
                                + toolkit.getLockingKeyState(KeyEvent.VK_CAPS_LOCK));
                    } catch (UnsupportedOperationException e) {
                        logger.severe("System does not allow to disable caps via toolkit: " + e.getMessage());
                    }

                    Thread.sleep(50);
                    boolean newState = toolkit.getLockingKeyState(KeyEvent.VK_CAPS_LOCK);
                    logger.info("Caps is " + (newState ? "enabled" : "disabled"));
                } else {
                    logger.fine("Caps is already disabled");
                }
            } catch (Exception e) {
                logger.severe("Error while disabling caps lock: " + e.getMessage());
            }
        }, 50, TimeUnit.MILLISECONDS);
    }

    public static void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}
