package com.example.filetransfer.server;

import com.example.filetransfer.ftp.FtpFileSender;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;

public class TcpFileServer implements Runnable {

    private final int port;
    private final Path root;
    private final FtpFileSender ftpSender;

    public TcpFileServer(int port, Path root, FtpFileSender ftpSender) {
        this.port = port;
        this.root = root;
        this.ftpSender = ftpSender;
    }

    @Override
    public void run() {
        System.out.println("[SERVER] TCP file server on port " + port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                Socket client = serverSocket.accept();
                new Thread(new ServerProcessor(client, root, ftpSender)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
