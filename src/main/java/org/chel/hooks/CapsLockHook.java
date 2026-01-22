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
        if (nativeEvent.getKeyCode() != NativeKeyEvent.VC_CAPS_LOCK) {
            return;
        }
        logger.info("Caps lock was pressed. Change lang");

        boolean shiftModifier = (nativeEvent.getModifiers() & NativeKeyEvent.SHIFT_MASK) != 0;
        logger.info("Checking if shift is on. Status is " + shiftModifier);

        if (shiftModifier) {
            logger.info("Caps+shift detected, keep default behaviour");
            return;
        }

        long now = System.currentTimeMillis();
        if (now - lastTrigger < 200) {
            logger.info("Repetitive caps lock trigger. Time passed: " + (now - lastTrigger));
            return;
        }

        lastTrigger = now;
        try {
            LangManager.changeLanguage();
            CapsManager.forceOffDelayed();

        } catch (Exception e) {
            logger.severe("Error while handling caps lock: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
