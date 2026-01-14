package org.chel.hooks;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.chel.managers.CapsManager;
import org.chel.managers.LangManager;

import java.util.logging.Logger;

public class CapsLockHook implements NativeKeyListener {
    private final Logger logger = Logger.getLogger(CapsLockHook.class.getName());
    private long lastTrigger = 0;

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_CAPS_LOCK) {
            long now = System.currentTimeMillis();
            if (now - lastTrigger < 200) {
                logger.info("Repetitive caps lock trigger. Time passed: " + (now - lastTrigger));
                return;
            }
            lastTrigger = now;
            logger.info("Caps lock was pressed. Change lang");
            try {
                LangManager.changeLangTest();
                CapsManager.forceOffDelayed();

            } catch (Exception e) {
                logger.severe("Error while handling caps lock: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
