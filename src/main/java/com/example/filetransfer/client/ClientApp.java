package com.example.filetransfer.client;

import com.example.filetransfer.backup.BackupService;
import com.example.filetransfer.backup.LocalBackupService;
import com.example.filetransfer.tcp.SimpleTcpFileSender;
import com.example.filetransfer.tcp.TcpFileSender;

public class ClientApp {

    public static void main(String[] args) {
        try {
            BackupService backupService = new LocalBackupService();
            TcpFileSender tcpSender = new SimpleTcpFileSender(ClientConfig.MAIN_SERVER_HOST, ClientConfig.MAIN_SERVER_PORT);
            LotContextManager lotManager = new LotContextManager(ClientConfig.WATCH_DIR, backupService, tcpSender);
            FileWatcher watcher = new FileWatcher(ClientConfig.WATCH_DIR, lotManager);
            watcher.run(); // 블로킹

        } catch (Exception e) {
            System.err.println("[CLIENT] Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
