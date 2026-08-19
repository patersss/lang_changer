package org.chel;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import org.chel.core.AppContext;
import org.chel.core.Lifecycle;
import org.chel.core.SchedulerDelayedRunner;
import org.chel.hooks.CapsLockHook;
import org.chel.platform.PlatformDetector;
import org.chel.platform.PlatformSupport;
import org.chel.ui.ProgramUI;
import org.chel.ui.SingleInstanceLock;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());

    static void main() {
        Lifecycle lifecycle = new Lifecycle();
        lifecycle.installShutdownHook();

        PlatformSupport platform;
        try {
            platform = PlatformDetector.detect();
        } catch (PlatformDetector.UnsupportedPlatformException e) {
            logger.severe(e.getMessage());
            System.exit(1);
            return;
        }
        logger.info("Платформа: " + platform.name());

        AppContext context = platform.createContext();
        new SingleInstanceLock(context.appPaths()).ensureSingleInstanceOrExit(lifecycle);

        SchedulerDelayedRunner delayedRunner = new SchedulerDelayedRunner("Caps-Manager");
        lifecycle.register("delayed runner", delayedRunner::close);

        try {
            Logger.getLogger(GlobalScreen.class.getPackageName()).setLevel(Level.WARNING);

            GlobalScreen.registerNativeHook();
            lifecycle.register("native hook", GlobalScreen::unregisterNativeHook);

            GlobalScreen.addNativeKeyListener(new CapsLockHook(
                    context.layoutSwitcher(),
                    context.capsLockController(),
                    delayedRunner));
            logger.info("Хук на Caps Lock установлен");

            new ProgramUI(lifecycle);
        } catch (NativeHookException e) {
            logger.severe("Не удалось установить хук на Caps Lock: " + e.getMessage());
            System.exit(1);
        }
    }
}
