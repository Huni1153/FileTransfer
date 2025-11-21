package com.example.filetransfer.client;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

public class FileWatcher implements Runnable {

    private final Path watchDir;
    private final LotContextManager lotContextManager;

    public FileWatcher(Path watchDir, LotContextManager lotContextManager) {
        this.watchDir = watchDir;
        this.lotContextManager = lotContextManager;
    }

    @Override
    public void run() {
        System.out.println("[WATCH] Start watching: " + watchDir);
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {

            watchDir.register(watchService, ENTRY_CREATE);

            while (true) {
                WatchKey key;
                try {
                    key = watchService.take();
                } catch (InterruptedException e) {
                    System.out.println("[WATCH] Interrupted, stop.");
                    Thread.currentThread().interrupt();
                    break;
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind<?> kind = event.kind();

                    if (kind == StandardWatchEventKinds.OVERFLOW) {
                        continue;
                    }

                    if (kind == ENTRY_CREATE) {
                        @SuppressWarnings("unchecked")
                        WatchEvent<Path> ev = (WatchEvent<Path>) event;
                        Path relative = ev.context();
                        Path fullPath = watchDir.resolve(relative);
                        handleNewFile(fullPath);
                    }
                }

                boolean valid = key.reset();
                if (!valid) {
                    System.out.println("[WATCH] Key no longer valid, stop.");
                    break;
                }
            }

        } catch (IOException e) {
            System.err.println("[WATCH] Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void handleNewFile(Path fullPath) {
        try {
            if (Files.isDirectory(fullPath)) {
                return;
            }

            String filename = fullPath.getFileName().toString();

            if (LotFileNameParser.isMainCsv(filename)) {
                System.out.println("[WATCH] Main CSV created: " + fullPath);

                boolean stable = FileStabilityChecker.waitUntilStable(
                        fullPath,
                        ClientConfig.MAIN_EXCEL_STABLE_MILLIS,
                        ClientConfig.MAIN_EXCEL_MAX_WAIT_MILLIS);

                if (!stable) {
                    System.err.println("[WATCH] Main Excel not stable (timeout): " + fullPath);
                    return;
                }

                lotContextManager.onMainExcelStable(fullPath);
            } else {
                // 서브엑셀, MAP 등
                lotContextManager.onSubFileDetected(fullPath);
            }

        } catch (Exception e) {
            System.err.println("[WATCH] handleNewFile error: " + fullPath + " : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
