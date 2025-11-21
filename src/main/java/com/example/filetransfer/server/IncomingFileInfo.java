package com.example.filetransfer.server.model;

import java.nio.file.Path;

public class IncomingFileInfo {

    private final String serverPath;   // 클라이언트가 보낸 논리 경로 (예: "60P1T9/60P1T9.csv")
    private final long size;          // 바이트 크기
    private final Path savedPath;     // 실제 서버 파일 경로 (예: D:/server-inbox/60P1T9/60P1T9.csv)

    public IncomingFileInfo(String serverPath, long size, Path savedPath) {
        this.serverPath = serverPath;
        this.size = size;
        this.savedPath = savedPath;
    }

    public String getServerPath() {
        return serverPath;
    }

    public long getSize() {
        return size;
    }

    public Path getSavedPath() {
        return savedPath;
    }

    @Override
    public String toString() {
        return "IncomingFileInfo{" +
                "serverPath='" + serverPath + '\'' +
                ", size=" + size +
                ", savedPath=" + savedPath +
                '}';
    }
}
