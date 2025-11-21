package com.example.filetransfer.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

public class TcpFileServer implements Runnable {

    private final int port;
    private final Path inboxRoot;

    public TcpFileServer(int port, Path inboxRoot) {
        this.port = port;
        this.inboxRoot = inboxRoot;
    }

    @Override
    public void run() {
        System.out.println("[SERVER] Starting TCP file server on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket client = serverSocket.accept();
                // 간단히 새 스레드로 처리 (부하 적다고 가정)
                Thread t = new Thread(new ServerProcessor(client, inboxRoot),
                        "server-processor-" + client.getRemoteSocketAddress());
                t.setDaemon(true);
                t.start();
            }
        } catch (IOException e) {
            System.err.println("[SERVER] Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
