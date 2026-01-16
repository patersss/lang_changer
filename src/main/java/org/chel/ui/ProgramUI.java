package org.chel.ui;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import org.chel.managers.CapsManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

public class ProgramUI {
    private static final Logger logger = Logger.getLogger(ProgramUI.class.getName());

    public ProgramUI() {
        setupSystemTray();
    }

    private void setupSystemTray() {
        // Проверяем, поддерживается ли SystemTray
        if (!SystemTray.isSupported()) {
            logger.warning("SystemTray не поддерживается");
            return;
        }

        try {
            // Получаем системный трей
            SystemTray tray = SystemTray.getSystemTray();

            // Создаем иконку (простая иконка 16x16)
            Image icon = createTrayIcon();

            // Создаем popup menu
            TrayIcon trayIcon = getTrayIcon(icon);

            // Двойной клик по иконке - показать информацию
            trayIcon.addActionListener(_ -> {
                JOptionPane.showMessageDialog(null,
                        "Приложение работает в фоновом режиме.\n\n" +
                                "Нажмите Caps Lock для переключения языка.",
                        "Caps Lock Switcher",
                        JOptionPane.INFORMATION_MESSAGE);
            });

            // Добавляем иконку в трей
            tray.add(trayIcon);
            logger.info("Системный трей настроен");

        } catch (AWTException e) {
            logger.severe("Не удалось добавить иконку в системный трей: " + e.getMessage());
        }
    }

    private TrayIcon getTrayIcon(Image icon) {
        PopupMenu popup = new PopupMenu();

        // Пункт меню: О программе
        MenuItem aboutItem = new MenuItem("О программе");
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(null,
                    "Capsyyyy. Программа для смены языка и не только(в будущем)\n\n" +
                            "Нажмите Caps Lock для переключения языка.\n" +
                            "Caps Lock автоматически отключается(практически сразу и практически без багов).",
                    "О программе",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        popup.add(aboutItem);

        // Пункт меню: Выход
        MenuItem exitItem = new MenuItem("Выход");
        exitItem.addActionListener(e -> {
            logger.info("Выход из приложения...");
            try {
                GlobalScreen.unregisterNativeHook();
                CapsManager.shutdown();
            } catch (NativeHookException ex) {
                logger.warning("Ошибка при отключении hook: " + ex.getMessage());
            }
            System.exit(0);
        });
        popup.add(exitItem);

        // Создаем TrayIcon
        TrayIcon trayIcon = new TrayIcon(icon, "Caps Lock Language Switcher", popup);
        trayIcon.setImageAutoSize(true); // Автоматический размер иконки
        return trayIcon;
    }

    private Image createTrayIcon() {
        // Создаем простую иконку 16x16 с буквой "C"
        int size = 16;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        // Включаем сглаживание
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Рисуем синий круг
        g.setColor(new Color(0, 120, 215)); // Windows blue
        g.fillOval(0, 0, size, size);

        // Рисуем белую букву "C"
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g.getFontMetrics();
        String text = "C";
        int x = (size - fm.stringWidth(text)) / 2;
        int y = ((size - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(text, x, y);

        g.dispose();
        return image;
    }
}
