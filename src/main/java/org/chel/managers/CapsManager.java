package org.chel.managers;

import com.sun.jna.platform.win32.BaseTSD;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import org.chel.jna.User32Ex;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.sun.jna.platform.win32.WinUser.*;

public class CapsManager {
    private static final Logger logger = Logger.getLogger(CapsManager.class.getName());
    private static final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "Caps-Manager");
        thread.setDaemon(true);
        return thread;
    });
    private static final int VK_CAPITAL = 0x14;
    private static final User32Ex user32 = User32Ex.INSTANCE;

    private static boolean isCapsLockOn() {
        // GetKeyState возвращает short, где младший бит = состояние toggle
        // Если младший бит = 1, то Caps Lock включен
        short state = user32.GetKeyState(VK_CAPITAL);
        return (state & 1) != 0;
    }

    public static void forceOffDelayed() {
        scheduler.schedule(() -> {
            try {
                logger.info("=== Starting Caps Lock disable procedure ===");

                // Проверяем начальное состояние через Windows API
                boolean initialState = isCapsLockOn();
                logger.info("Initial Caps Lock state (Windows API): " + (initialState ? "ON ✓" : "OFF ✗"));

                if (initialState) {
                    logger.info("Caps Lock is ON. Sending keypress via SendInput...");

                    // Используем SendInput для эмуляции нажатия
                    sendCapsLockPress();

                    // Даем время системе обработать
                    Thread.sleep(100);

                    // Проверяем результат
                    boolean finalState = isCapsLockOn();
                    logger.info("Final Caps Lock state (Windows API): " + (finalState ? "ON ✗" : "OFF ✓"));

                    if (finalState) {
                        logger.severe("⚠️ FAILED: Caps Lock is still ON after toggle!");

                        // Повторная попытка
                        logger.info("Retrying...");
                        sendCapsLockPress();
                        Thread.sleep(100);

                        boolean retryState = isCapsLockOn();
                        logger.info("After retry: " + (retryState ? "ON ✗" : "OFF ✓"));
                    } else {
                        logger.info("✅ SUCCESS: Caps Lock turned OFF");
                    }
                } else {
                    logger.info("Caps Lock is already OFF, nothing to do");
                }
                logger.info("=== Caps Lock procedure complete ===");
            } catch (InterruptedException e) {
                logger.warning("Sleep interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.severe("Error while managing Caps Lock: " + e.getMessage());
                e.printStackTrace();
            }
        }, 100, TimeUnit.MILLISECONDS);
    }

    /**
     * Отправляет нажатие Caps Lock через SendInput (самый надежный метод)
     */
    private static void sendCapsLockPress() {

        INPUT input = new INPUT();
        INPUT[] inputs = (INPUT[]) input.toArray(2);

        // KEY DOWN
        inputs[0].type = new DWORD(INPUT.INPUT_KEYBOARD);
        inputs[0].input.setType("ki");
        inputs[0].input.ki.wVk = new WinDef.WORD(VK_CAPITAL);
        inputs[0].input.ki.wScan = new WinDef.WORD(0);
        inputs[0].input.ki.dwFlags = new DWORD(0);
        inputs[0].input.ki.time = new DWORD(0);
        inputs[0].input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);

        // KEY UP
        inputs[1].type = new DWORD(INPUT.INPUT_KEYBOARD);
        inputs[1].input.setType("ki");
        inputs[1].input.ki.wVk = new WinDef.WORD(VK_CAPITAL);
        inputs[1].input.ki.wScan = new WinDef.WORD(0);
        inputs[1].input.ki.dwFlags = new DWORD(KEYBDINPUT.KEYEVENTF_KEYUP);
        inputs[1].input.ki.time = new DWORD(0);
        inputs[1].input.ki.dwExtraInfo = new BaseTSD.ULONG_PTR(0);

        User32.INSTANCE.SendInput(
                new DWORD(inputs.length),
                inputs,
                inputs[0].size()
        );

        logger.info("SendInput executed for Caps Lock");
    }


    public static void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
