package org.chel;

import org.chel.core.AppContext;
import org.chel.core.KeyboardHook;
import org.chel.core.Lifecycle;
import org.chel.hooks.CapsLockHandler;
import org.chel.platform.PlatformDetector;
import org.chel.platform.PlatformSupport;
import org.chel.ui.ProgramUI;
import org.chel.ui.SingleInstanceLock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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

        if (context.hasFullPrivileges()) {
            logger.info("Процесс запущен с повышенными правами: работает и поверх окон администратора");
        } else {
            logger.warning("Процесс запущен без повышения прав. Поверх окон с правами администратора " +
                    "(консоль от админа, диспетчер задач, диалоги UAC) переключение работать не будет — " +
                    "так устроен UIPI. См. readme, раздел про запуск с правами администратора.");
        }

        // смена раскладки уходит сюда, чтобы колбэк хука возвращался мгновенно
        ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable, "Layout-Switch");
            thread.setDaemon(true);
            return thread;
        });
        lifecycle.register("layout worker", worker::close);

        KeyboardHook hook = context.keyboardHook();
        lifecycle.register("keyboard hook", hook::close);
        hook.start(new CapsLockHandler(context.layoutSwitcher(), worker));

        new ProgramUI(lifecycle, context.hasFullPrivileges());
    }
}
