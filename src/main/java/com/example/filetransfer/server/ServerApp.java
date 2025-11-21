package com.example.filetransfer.server;

public class ServerApp {

    public static void main(String[] args) {
        TcpFileServer server = new TcpFileServer(ServerConfig.PORT, ServerConfig.INBOX_ROOT);
        server.run(); // 블로킹
    }
}
