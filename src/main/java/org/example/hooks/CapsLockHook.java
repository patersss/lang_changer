package org.example.hooks;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import org.example.managers.CapsManager;
import org.example.managers.LangManager;

import java.util.logging.Logger;

public class CapsLockHook implements NativeKeyListener {
    private final Logger logger = Logger.getLogger(CapsLockHook.class.getName());
    private long lastTrigger = 0;
//    @Override
//    public void nativeKeyReleased(NativeKeyEvent nativeEvent) {
//        if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_CAPS_LOCK) {
//            long now = System.currentTimeMillis();
//            if (now - lastTrigger < 150) {
//                logger.info("Repetitive caps lock trigger. Less then 150 microseconds passed.");
//                return;
//            }
//            lastTrigger = now;
//            logger.info("Caps lock was released");
//            LangManager.changeLang();
//            CapsManager.forceOffDelayed();
//        }
//    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent nativeEvent) {
        if (nativeEvent.getKeyCode() == NativeKeyEvent.VC_CAPS_LOCK) {
            long now = System.currentTimeMillis();
            if (now - lastTrigger < 300) {
                logger.info("Repetitive caps lock trigger. Time passed: " + (now - lastTrigger));
                return;
            }
            lastTrigger = now;
            logger.info("Caps lock was pressed. Change lang");
            try {
                LangManager.changeLang();
                CapsManager.forceOffDelayed();

            } catch (Exception e) {
                logger.severe("Error while handling caps lock: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
