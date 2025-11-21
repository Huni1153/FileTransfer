package com.example.filetransfer.client;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class ClientConfig {

    private ClientConfig() {}

    // ★ 설비 데이터가 생기는 디렉토리 (테스트할 때 여기 바꾸면 됨)
    public static final Path WATCH_DIR = Paths.get("D:/test");

    // ★ 백업 루트
    public static final Path BACKUP_ROOT = Paths.get("D:/backup");

    // ★ 메인 서버 TCP 정보
    public static final String MAIN_SERVER_HOST = "127.0.0.1";
    public static final int MAIN_SERVER_PORT = 5000;

    // ★ 메인 엑셀 안정화 기준
    public static final long MAIN_EXCEL_STABLE_MILLIS = 3_000L;   // 수정 없음 3초 유지
    public static final long MAIN_EXCEL_MAX_WAIT_MILLIS = 30_000L; // 최대 30초 기다림

    // ★ 서브파일(서브엑셀/MAP) 안정 체크 시간
    public static final long SUBFILE_STABLE_MILLIS = 5_000L;     // 마지막 수정 후 5초 이상
}
