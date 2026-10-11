package org.chel.ui;

import org.chel.core.Lifecycle;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

public class ProgramUI {
    private static final Logger logger = Logger.getLogger(ProgramUI.class.getName());
    private static final String LIMITED_RIGHTS_NOTE =
            """
                    Приложение запущено без прав администратора.
                    Поверх окон с повышенными правами (консоль от админа,
                    диспетчер задач) переключение работать не будет.""";

    private final Lifecycle lifecycle;
    private final boolean hasFullPrivileges;

    public ProgramUI(Lifecycle lifecycle, boolean hasFullPrivileges) {
        this.lifecycle = lifecycle;
        this.hasFullPrivileges = hasFullPrivileges;
        setupSystemTray();
    }

    private void setupSystemTray() {
        setAppLookAndFeel();

        if (!SystemTray.isSupported()) {
            logger.warning("SystemTray не поддерживается");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();
            TrayIcon trayIcon = getTrayIcon(createTrayIcon());

            trayIcon.addActionListener(_ -> JOptionPane.showMessageDialog(null,
                    "Приложение работает в фоновом режиме.\n\n" +
                            "Нажмите Caps Lock для переключения языка." +
                            (hasFullPrivileges ? "" : "\n\n" + LIMITED_RIGHTS_NOTE),
                    "Caps Lock Switcher",
                    JOptionPane.INFORMATION_MESSAGE));

            tray.add(trayIcon);
            lifecycle.register("system tray icon", () -> tray.remove(trayIcon));
            logger.info("Системный трей настроен");

        } catch (AWTException e) {
            logger.severe("Не удалось добавить иконку в системный трей: " + e.getMessage());
        }
    }

    private TrayIcon getTrayIcon(Image icon) {
        PopupMenu popup = new PopupMenu();

        MenuItem aboutItem = getAboutMenuItem();
        popup.add(aboutItem);

        MenuItem exitItem = new MenuItem("Выход");
        // всю очистку делает Lifecycle через shutdown hook — здесь только выход
        exitItem.addActionListener(_ -> {
            logger.info("Выход из приложения...");
            System.exit(0);
        });
        popup.add(exitItem);

        TrayIcon trayIcon = new TrayIcon(icon, "Caps Lock Language Switcher", popup);
        trayIcon.setImageAutoSize(true);
        return trayIcon;
    }

    private MenuItem getAboutMenuItem() {
        MenuItem aboutItem = new MenuItem("О программе");
        aboutItem.addActionListener(_ -> JOptionPane.showMessageDialog(null,
                "Capsyyyy(версия " + AppInfo.version + "). Программа для смены языка и не только(в будущем)\n\n" +
                        "Нажмите Caps Lock для переключения языка.\n" +
                        "Сам Caps Lock при этом не срабатывает — нажатие поглощается.\n" +
                        "Shift+Caps Lock включает капс как обычно.\n\n" +
                        (hasFullPrivileges
                                ? "Права администратора: есть, работает во всех окнах."
                                : LIMITED_RIGHTS_NOTE),
                "О программе",
                hasFullPrivileges ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE));
        return aboutItem;
    }

    private Image createTrayIcon() {
        int size = 16;
        BufferedImage image = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0, 120, 215));
        g.fillOval(0, 0, size, size);

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

    private void setAppLookAndFeel() {
        String[] lookAndFeelCandidates = {
                UIManager.getSystemLookAndFeelClassName(),
                UIManager.getCrossPlatformLookAndFeelClassName()
        };

        for (String candidate : lookAndFeelCandidates) if (tryLookAndFeel(candidate)) return;
    }

    private boolean tryLookAndFeel(String classname) {
        try {
            UIManager.setLookAndFeel(classname);
            return true;
        } catch (UnsupportedLookAndFeelException | ClassNotFoundException | InstantiationException |
                 IllegalAccessException e) {
            logger.severe("Error while setting look and feel: classname - " + classname);
            return false;
        }
    }
}
