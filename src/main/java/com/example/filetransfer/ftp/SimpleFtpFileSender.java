package com.example.filetransfer.ftp;

import java.io.IOException;
import java.nio.file.Path;

public class SimpleFtpFileSender implements FtpFileSender {

    private final String host;
    private final int port;
    private final String user;
    private final String password;

    public SimpleFtpFileSender(String host, int port, String user, String password) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
    }

    @Override
    public void upload(Path localFile, String remotePath) throws IOException {
        // TODO: 실제 FTP 연동 구현은 나중에 할 것.
        // 지금은 로그만 찍고 지나간다.
        System.out.println("[FTP] Upload request: local=" + localFile
                + ", remote=" + remotePath
                + " (host=" + host + ":" + port + ", user=" + user + ")");
    }
}
