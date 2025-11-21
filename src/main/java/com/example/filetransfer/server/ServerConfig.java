package com.example.filetransfer.server;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ServerConfig {

    private ServerConfig() {}

    /** 클라이언트가 붙는 포트 (ClientConfig.MAIN_SERVER_PORT와 동일하게) */
    public static final int PORT = 5000;

    /**
     * 클라이언트에서 보내온 serverPath(예: "60P1T9/60P1T9.csv") 를
     * 이 디렉토리 아래에 저장한다.
     *
     * 예: INBOX_ROOT/60P1T9/60P1T9.csv
     */
    public static final Path INBOX_ROOT = Paths.get("D:/server-inbox");

    // 나중에 L01 + 최종 결과 디렉토리, FTP 설정 등도 여기에 추가할 예정
}
