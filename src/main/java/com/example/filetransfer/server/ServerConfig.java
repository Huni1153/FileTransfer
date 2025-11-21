package com.example.filetransfer.server;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ServerConfig {
    private ServerConfig() {}

    // 메인서버 정보
    public static final int PORT = 5000;
    public static final Path SERVER_ROOT = Paths.get("D:/"); // 서버 디렉토리 경로
    
    // FTP서버 정보
    public static final String FTP_HOST = "222.121.122.90";
    public static final int FTP_PORT = 21;
    public static final String FTP_USER = "user";
    public static final String FTP_PASSWORD = "password";
    public static final String FTP_BASE_DIR = "/"; // FTP서버 디렉토리 경로
}
