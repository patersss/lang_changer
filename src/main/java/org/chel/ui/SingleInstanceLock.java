package org.chel.ui;

import org.chel.core.AppPaths;
import org.chel.core.Lifecycle;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

/**
 * Блокировка файла в каталоге данных приложения: второй экземпляр не получит lock и завершится.
 */
public class SingleInstanceLock {
    private static final Logger logger = Logger.getLogger(SingleInstanceLock.class.getName());
    private static final String LOCK_FILE_NAME = "app.lock";

    private final AppPaths appPaths;
    private FileChannel fileChannel;
    private FileLock lock;

    public SingleInstanceLock(AppPaths appPaths) {
        this.appPaths = appPaths;
    }

    /**
     * @return true, если этот экземпляр единственный; false — если приложение уже запущено
     */
    public boolean tryAcquire() throws IOException {
        Path programDirectory = appPaths.appDataDirectory();
        Files.createDirectories(programDirectory);

        Path lockFile = programDirectory.resolve(LOCK_FILE_NAME);
        fileChannel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        lock = fileChannel.tryLock();

        if (lock == null) {
            release();
            return false;
        }
        return true;
    }

    public void release() {
        try {
            if (lock != null) {
                lock.release();
                lock = null;
            }
        } catch (IOException e) {
            logger.warning("Не удалось снять блокировку: " + e.getMessage());
        }
        try {
            if (fileChannel != null) {
                fileChannel.close();
                fileChannel = null;
            }
        } catch (IOException e) {
            logger.warning("Не удалось закрыть lock-файл: " + e.getMessage());
        }
    }

    /**
     * Завершает процесс, если приложение уже запущено. Иначе регистрирует снятие блокировки.
     */
    public void ensureSingleInstanceOrExit(Lifecycle lifecycle) {
        logger.info("Проверяем, не запущен ли уже экземпляр приложения");
        try {
            if (!tryAcquire()) {
                logger.info("Приложение уже запущено, выходим");
                System.exit(0);
            }
            lifecycle.register("single instance lock", this::release);
        } catch (IOException e) {
            logger.severe("Ошибка при создании lock-файла: " + e.getMessage());
            System.exit(1);
        }
    }
}
