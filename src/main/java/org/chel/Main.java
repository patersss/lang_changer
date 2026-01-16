package org.chel;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import org.chel.hooks.CapsLockHook;
import org.chel.managers.CapsManager;
import org.chel.ui.ProgramUI;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    static void main() {

        try {
            Logger jNativeLogger = Logger.getLogger(GlobalScreen.class.getPackageName());
            jNativeLogger.setLevel(Level.WARNING);

            GlobalScreen.registerNativeHook();
            GlobalScreen.addNativeKeyListener(new CapsLockHook());
            logger.info("Set hook and listener for Caps Lock");

            ProgramUI ui = new ProgramUI();

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    GlobalScreen.unregisterNativeHook();
                    CapsManager.shutdown();
                    logger.info("Delete hook for caps lock");
                } catch (NativeHookException ex) {
                    logger.severe("Error while deleting hook for caps lock: " + ex.getMessage());
                }
            }));
        } catch (NativeHookException ex) {
            logger.severe("Error while setting hook for caps lock: " + ex.getMessage());
            System.exit(1);
        }
    }
}
