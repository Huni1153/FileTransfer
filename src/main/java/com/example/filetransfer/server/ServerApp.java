package com.example.filetransfer.server;

import com.example.filetransfer.ftp.FtpFileSender;
import com.example.filetransfer.ftp.SimpleFtpFileSender;

public class ServerApp {

    public static void main(String[] args) {

        FtpFileSender ftpSender = new SimpleFtpFileSender(
                ServerConfig.FTP_HOST,
                ServerConfig.FTP_PORT,
                ServerConfig.FTP_USER,
                ServerConfig.FTP_PASSWORD
        );

        TcpFileServer server = new TcpFileServer(ServerConfig.PORT, ServerConfig.SERVER_ROOT, ftpSender);
        server.run();
    }
}
