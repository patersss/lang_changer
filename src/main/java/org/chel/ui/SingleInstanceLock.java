package org.chel.ui;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.Logger;

public class SingleInstanceLock {
    private static final Logger logger = Logger.getLogger(SingleInstanceLock.class.getName());
    private FileChannel fileChannel;
    private FileLock lock;

    public void ensureSingleInstanceOrExit() {
        logger.info("Checking whether instance of program is already running or not");
        try {
            Path programDirectory = Paths.get(System.getenv("LOCALAPPDATA"), "Capsy");
            Files.createDirectories(programDirectory);

            Path lockFile = programDirectory.resolve("app.lock");
            fileChannel = FileChannel.open(lockFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

            lock = fileChannel.tryLock();
            if (lock == null) {
                System.exit(0);
            }

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try { if (lock != null) lock.release(); } catch (IOException _) {}
                try { if (fileChannel != null) fileChannel.close(); } catch (IOException _) {}
            }));
        } catch (IOException e) {
            logger.severe("Error while setting lock file of program");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
